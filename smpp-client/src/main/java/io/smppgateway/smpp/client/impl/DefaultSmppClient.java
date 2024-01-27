package io.smppgateway.smpp.client.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.smppgateway.smpp.client.*;
import io.smppgateway.smpp.exception.SmppException;
import io.smppgateway.smpp.netty.codec.*;
import io.smppgateway.smpp.netty.window.Window;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.types.CommandStatus;
import io.smppgateway.smpp.types.SmppBindType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default implementation of SmppClient with auto-reconnect support.
 */
public class DefaultSmppClient implements SmppClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultSmppClient.class);

    private final SmppClientConfiguration config;
    private final SmppClientHandler handler;

    private EventLoopGroup eventLoopGroup;
    private ExecutorService handlerExecutor;
    private ScheduledExecutorService reconnectScheduler;

    private volatile SmppClientSession session;
    private volatile Channel channel;

    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private final AtomicBoolean shouldReconnect = new AtomicBoolean(true);
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    public DefaultSmppClient(SmppClientConfiguration config, SmppClientHandler handler) {
        this.config = config;
        this.handler = handler;
    }

    @Override
    public SmppClientSession connect() throws Exception {
        try {
            return connectAsync().get(
                config.connectTimeout().toMillis() + config.bindTimeout().toMillis(),
                TimeUnit.MILLISECONDS
            );
        } catch (ExecutionException e) {
            throw (Exception) e.getCause();
        }
    }

    @Override
    public CompletableFuture<SmppClientSession> connectAsync() {
        if (!connecting.compareAndSet(false, true)) {
            return CompletableFuture.failedFuture(
                new SmppException("Already connecting"));
        }

        CompletableFuture<SmppClientSession> future = new CompletableFuture<>();
        shouldReconnect.set(true);

        try {
            // Initialize resources
            if (eventLoopGroup == null || eventLoopGroup.isShutdown()) {
                eventLoopGroup = new NioEventLoopGroup();
            }

            if (handlerExecutor == null || handlerExecutor.isShutdown()) {
                handlerExecutor = Executors.newVirtualThreadPerTaskExecutor();
            }

            if (reconnectScheduler == null || reconnectScheduler.isShutdown()) {
                reconnectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "smpp-client-reconnect");
                    t.setDaemon(true);
                    return t;
                });
            }

            doConnect(future);

        } catch (Exception e) {
            connecting.set(false);
            future.completeExceptionally(e);
        }

        return future;
    }

    private void doConnect(CompletableFuture<SmppClientSession> future) {
        Window<PduResponse> window = new Window<>(config.windowSize(), config.requestTimeout());

        SmppClientSessionHandler sessionHandler = new SmppClientSessionHandler(
            handler, handlerExecutor, this::handleDisconnect);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) config.connectTimeout().toMillis())
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    // TLS if configured
                    if (config.tlsConfig() != null) {
                        pipeline.addLast("ssl",
                            config.tlsConfig().sslContext().newHandler(ch.alloc()));
                    }

                    // Idle state handler
                    long readIdleMs = config.enquireLinkInterval().toMillis() * 3;
                    long writeIdleMs = config.enquireLinkInterval().toMillis();
                    pipeline.addLast("idleState", new IdleStateHandler(
                        readIdleMs, writeIdleMs, 0, TimeUnit.MILLISECONDS));

                    // SMPP codec
                    pipeline.addLast("frameDecoder", new SmppFrameDecoder(config.maxPduSize()));
                    pipeline.addLast("pduDecoder", new SmppPduDecoder());
                    pipeline.addLast("pduEncoder", new SmppPduEncoder());

                    // Session handler
                    pipeline.addLast("sessionHandler", sessionHandler);
                }
            });

        InetSocketAddress address = new InetSocketAddress(config.host(), config.port());

        bootstrap.connect(address).addListener((ChannelFutureListener) connectFuture -> {
            if (connectFuture.isSuccess()) {
                channel = connectFuture.channel();
                log.info("Connected to {}:{}", config.host(), config.port());

                // Create session
                session = new SmppClientSession(channel, window, config);
                sessionHandler.setSession(session);

                // Send bind request
                doBind(future, window);
            } else {
                log.error("Failed to connect to {}:{}", config.host(), config.port(),
                         connectFuture.cause());
                connecting.set(false);
                future.completeExceptionally(connectFuture.cause());
            }
        });
    }

    private void doBind(CompletableFuture<SmppClientSession> future, Window<PduResponse> window) {
        int seq = session.nextSequenceNumber();

        PduRequest<?> bindRequest = switch (config.bindType()) {
            case TRANSMITTER -> new BindTransmitter(seq, config.systemId(), config.password(),
                config.systemType(), config.interfaceVersion(), config.addressTon(),
                config.addressNpi(), config.addressRange(), List.of());
            case RECEIVER -> new BindReceiver(seq, config.systemId(), config.password(),
                config.systemType(), config.interfaceVersion(), config.addressTon(),
                config.addressNpi(), config.addressRange(), List.of());
            case TRANSCEIVER -> new BindTransceiver(seq, config.systemId(), config.password(),
                config.systemType(), config.interfaceVersion(), config.addressTon(),
                config.addressNpi(), config.addressRange(), List.of());
        };

        // Set up response handling
        channel.pipeline().addFirst("bindHandler", new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof PduResponse resp && resp.sequenceNumber() == seq) {
                    ctx.pipeline().remove(this);
                    handleBindResponse(resp, future);
                } else {
                    super.channelRead(ctx, msg);
                }
            }
        });

        // Send bind
        channel.writeAndFlush(bindRequest).addListener(f -> {
            if (!f.isSuccess()) {
                connecting.set(false);
                future.completeExceptionally(f.cause());
            }
        });

        // Set bind timeout
        reconnectScheduler.schedule(() -> {
            if (!future.isDone()) {
                connecting.set(false);
                future.completeExceptionally(
                    new TimeoutException("Bind timeout"));
                channel.close();
            }
        }, config.bindTimeout().toMillis(), TimeUnit.MILLISECONDS);
    }

    private void handleBindResponse(PduResponse response, CompletableFuture<SmppClientSession> future) {
        connecting.set(false);
        reconnectAttempts.set(0);

        if (response.commandStatus() == CommandStatus.ESME_ROK) {
            String serverSystemId = switch (response) {
                case BindReceiverResp resp -> resp.systemId();
                case BindTransmitterResp resp -> resp.systemId();
                case BindTransceiverResp resp -> resp.systemId();
                default -> "";
            };

            session.setBound(config.bindType(), serverSystemId);
            log.info("Bound to {} as {} ({})", config.host(), config.systemId(), config.bindType());

            handlerExecutor.execute(() -> {
                try {
                    handler.sessionBound(session);
                } catch (Exception e) {
                    log.error("Error in sessionBound handler", e);
                }
            });

            future.complete(session);
        } else {
            log.error("Bind rejected: {}", response.commandStatus());
            channel.close();
            future.completeExceptionally(
                new SmppException(response.commandStatus(), "Bind rejected"));
        }
    }

    private void handleDisconnect(Throwable cause) {
        log.warn("Disconnected: {}", cause != null ? cause.getMessage() : "unknown");

        SmppClientSession oldSession = session;
        session = null;
        channel = null;

        handlerExecutor.execute(() -> {
            try {
                handler.connectionLost(oldSession, cause);
            } catch (Exception e) {
                log.error("Error in connectionLost handler", e);
            }
        });

        // Attempt reconnect if enabled
        if (shouldReconnect.get() && shouldAttemptReconnect()) {
            scheduleReconnect();
        }
    }

    private boolean shouldAttemptReconnect() {
        int maxAttempts = config.maxReconnectAttempts();
        if (maxAttempts == 0) {
            return false;
        }
        if (maxAttempts < 0) {
            return true; // Infinite
        }
        return reconnectAttempts.get() < maxAttempts;
    }

    private void scheduleReconnect() {
        int attempt = reconnectAttempts.incrementAndGet();

        handlerExecutor.execute(() -> {
            try {
                handler.reconnecting(attempt, config.reconnectDelay());
            } catch (Exception e) {
                log.error("Error in reconnecting handler", e);
            }
        });

        log.info("Scheduling reconnect attempt {} in {}",
                attempt, config.reconnectDelay());

        reconnectScheduler.schedule(() -> {
            if (shouldReconnect.get()) {
                connectAsync().thenAccept(newSession -> {
                    handlerExecutor.execute(() -> {
                        try {
                            handler.reconnected(newSession, attempt);
                        } catch (Exception e) {
                            log.error("Error in reconnected handler", e);
                        }
                    });
                }).exceptionally(e -> {
                    log.error("Reconnect attempt {} failed", attempt, e);
                    if (shouldAttemptReconnect()) {
                        scheduleReconnect();
                    }
                    return null;
                });
            }
        }, config.reconnectDelay().toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void disconnect() {
        try {
            disconnectAsync().get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Error during disconnect", e);
        }
    }

    @Override
    public CompletableFuture<Void> disconnectAsync() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        shouldReconnect.set(false);

        if (session != null) {
            try {
                session.unbind();
            } catch (Exception e) {
                log.warn("Error sending unbind", e);
            }
        }

        if (channel != null && channel.isActive()) {
            channel.close().addListener(f -> {
                cleanup();
                future.complete(null);
            });
        } else {
            cleanup();
            future.complete(null);
        }

        return future;
    }

    private void cleanup() {
        if (reconnectScheduler != null && !reconnectScheduler.isShutdown()) {
            reconnectScheduler.shutdownNow();
        }
        if (handlerExecutor != null && !handlerExecutor.isShutdown()) {
            handlerExecutor.shutdownNow();
        }
        if (eventLoopGroup != null && !eventLoopGroup.isShutdown()) {
            eventLoopGroup.shutdownGracefully();
        }
    }

    @Override
    public SmppClientSession getSession() {
        return session;
    }

    @Override
    public boolean isConnected() {
        return session != null && session.isBound() && session.isActive();
    }

    @Override
    public SmppClientConfiguration getConfiguration() {
        return config;
    }
}
