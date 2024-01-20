package io.smppgateway.smpp.server.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.smppgateway.smpp.netty.codec.SmppFrameDecoder;
import io.smppgateway.smpp.netty.codec.SmppPduDecoder;
import io.smppgateway.smpp.netty.codec.SmppPduEncoder;
import io.smppgateway.smpp.server.SmppServer;
import io.smppgateway.smpp.server.SmppServerConfiguration;
import io.smppgateway.smpp.server.SmppServerHandler;
import io.smppgateway.smpp.server.SmppServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Default implementation of SmppServer using Netty.
 */
public class DefaultSmppServer implements SmppServer {

    private static final Logger log = LoggerFactory.getLogger(DefaultSmppServer.class);

    private final SmppServerConfiguration config;
    private final SmppServerHandler handler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private ExecutorService handlerExecutor;

    private final ConcurrentMap<String, SmppServerSession> sessions = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    public DefaultSmppServer(SmppServerConfiguration config, SmppServerHandler handler) {
        this.config = config;
        this.handler = handler;
    }

    @Override
    public CompletableFuture<Void> start() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            // Create executor for handler callbacks
            if (config.useVirtualThreads()) {
                handlerExecutor = VirtualThreadExecutor.createForServer();
            } else {
                handlerExecutor = Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors() * 2,
                    r -> {
                        Thread t = new Thread(r);
                        t.setName("smpp-server-handler-" + t.getId());
                        t.setDaemon(true);
                        return t;
                    }
                );
            }

            // Create Netty event loops
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        configurePipeline(ch);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, config.maxConnections())
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

            // Bind and start
            InetSocketAddress bindAddress = new InetSocketAddress(config.host(), config.port());

            bootstrap.bind(bindAddress).addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    serverChannel = f.channel();
                    running = true;
                    log.info("SMPP server started on {}:{}", config.host(), config.port());
                    future.complete(null);
                } else {
                    log.error("Failed to start SMPP server on {}:{}", config.host(), config.port(), f.cause());
                    cleanup();
                    future.completeExceptionally(f.cause());
                }
            });

        } catch (Exception e) {
            log.error("Failed to start SMPP server", e);
            cleanup();
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public void startSync() {
        try {
            start().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while starting server", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to start server", e.getCause());
        }
    }

    @Override
    public CompletableFuture<Void> stop() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (!running) {
            future.complete(null);
            return future;
        }

        log.info("Stopping SMPP server...");

        // Close all sessions
        for (SmppServerSession session : sessions.values()) {
            try {
                session.close();
            } catch (Exception e) {
                log.warn("Error closing session {}", session.getSessionId(), e);
            }
        }

        // Close server channel
        if (serverChannel != null) {
            serverChannel.close().addListener(f -> {
                cleanup();
                running = false;
                log.info("SMPP server stopped");
                future.complete(null);
            });
        } else {
            cleanup();
            running = false;
            future.complete(null);
        }

        return future;
    }

    @Override
    public void stopSync() {
        try {
            stop().get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while stopping server", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to stop server", e.getCause());
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout while stopping server", e);
        }
    }

    private void cleanup() {
        if (handlerExecutor != null) {
            handlerExecutor.shutdownNow();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    private void configurePipeline(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // TLS if configured
        if (config.tlsConfig() != null) {
            pipeline.addLast("ssl", config.tlsConfig().sslContext().newHandler(ch.alloc()));
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
        pipeline.addLast("sessionHandler", new SmppSessionHandler(
            handler,
            handlerExecutor,
            config.systemId(),
            this::registerSession,
            this::unregisterSession
        ));
    }

    private void registerSession(SmppServerSession session) {
        sessions.put(session.getSessionId(), session);
        log.debug("Registered session: {} (total: {})", session.getSessionId(), sessions.size());
    }

    private void unregisterSession(SmppServerSession session) {
        sessions.remove(session.getSessionId());
        log.debug("Unregistered session: {} (total: {})", session.getSessionId(), sessions.size());
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPort() {
        return config.port();
    }

    @Override
    public Collection<SmppServerSession> getSessions() {
        return sessions.values();
    }

    @Override
    public SmppServerSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public int getSessionCount() {
        return sessions.size();
    }

    @Override
    public SmppServerConfiguration getConfiguration() {
        return config;
    }
}
