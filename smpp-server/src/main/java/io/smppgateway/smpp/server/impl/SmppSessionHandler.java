package io.smppgateway.smpp.server.impl;

import io.netty.channel.ChannelHandlerContext;
import io.smppgateway.smpp.netty.handler.SmppChannelHandler;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.server.SmppServerHandler;
import io.smppgateway.smpp.server.SmppServerHandler.*;
import io.smppgateway.smpp.server.SmppServerSession;
import io.smppgateway.smpp.types.CommandStatus;
import io.smppgateway.smpp.types.SmppBindType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Netty channel handler for server-side SMPP sessions.
 *
 * <p>This handler processes incoming PDUs and delegates to the user's
 * SmppServerHandler for business logic. Handler callbacks run on virtual
 * threads for high concurrency.
 */
public class SmppSessionHandler extends SmppChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(SmppSessionHandler.class);

    private final SmppServerHandler handler;
    private final Executor executor;
    private final String serverSystemId;
    private final Consumer<SmppServerSession> onSessionCreated;
    private final Consumer<SmppServerSession> onSessionDestroyed;

    private SmppServerSession session;

    public SmppSessionHandler(SmppServerHandler handler, Executor executor, String serverSystemId,
                              Consumer<SmppServerSession> onSessionCreated,
                              Consumer<SmppServerSession> onSessionDestroyed) {
        this.handler = handler;
        this.executor = executor;
        this.serverSystemId = serverSystemId;
        this.onSessionCreated = onSessionCreated;
        this.onSessionDestroyed = onSessionDestroyed;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        // Create session
        session = new SmppServerSession(ctx.channel());
        onSessionCreated.accept(session);

        // Notify handler
        executor.execute(() -> {
            try {
                handler.sessionCreated(session);
            } catch (Exception e) {
                log.error("Error in sessionCreated handler", e);
            }
        });

        log.info("Session created: {}", session.getSessionId());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        if (session != null) {
            session.setUnbound();
            onSessionDestroyed.accept(session);

            executor.execute(() -> {
                try {
                    handler.sessionDestroyed(session);
                } catch (Exception e) {
                    log.error("Error in sessionDestroyed handler", e);
                }
            });

            log.info("Session destroyed: {}", session.getSessionId());
        }
    }

    @Override
    protected void handleBind(ChannelHandlerContext ctx, PduRequest<?> bindRequest, SmppBindType bindType) {
        executor.execute(() -> {
            try {
                // Extract credentials
                String systemId;
                String password;
                String systemType;
                byte interfaceVersion;

                switch (bindRequest) {
                    case BindReceiver bind -> {
                        systemId = bind.systemId();
                        password = bind.password();
                        systemType = bind.systemType();
                        interfaceVersion = bind.interfaceVersion();
                    }
                    case BindTransmitter bind -> {
                        systemId = bind.systemId();
                        password = bind.password();
                        systemType = bind.systemType();
                        interfaceVersion = bind.interfaceVersion();
                    }
                    case BindTransceiver bind -> {
                        systemId = bind.systemId();
                        password = bind.password();
                        systemType = bind.systemType();
                        interfaceVersion = bind.interfaceVersion();
                    }
                    default -> throw new IllegalArgumentException("Unexpected bind type");
                }

                log.debug("Processing bind request: systemId={} bindType={}", systemId, bindType);

                // Authenticate
                BindResult result = handler.authenticate(session, systemId, password, bindRequest);

                // Build response
                String responseSystemId = result.systemId() != null ?
                    result.systemId() : serverSystemId;

                PduResponse response = switch (bindType) {
                    case RECEIVER -> new BindReceiverResp(bindRequest.sequenceNumber(),
                        result.status(), responseSystemId, List.of());
                    case TRANSMITTER -> new BindTransmitterResp(bindRequest.sequenceNumber(),
                        result.status(), responseSystemId, List.of());
                    case TRANSCEIVER -> new BindTransceiverResp(bindRequest.sequenceNumber(),
                        result.status(), responseSystemId, List.of());
                };

                ctx.writeAndFlush(response);

                if (result.isSuccess()) {
                    session.setBound(bindType, systemId, password, systemType, interfaceVersion);
                    stateMachine.onBind(bindType);

                    handler.sessionBound(session);
                    log.info("Session bound: {} systemId={} bindType={}",
                            session.getSessionId(), systemId, bindType);
                } else {
                    log.warn("Bind rejected: systemId={} status={}", systemId, result.status());
                    ctx.close();
                }
            } catch (Exception e) {
                log.error("Error processing bind request", e);
                session.incrementErrors();
                ctx.close();
            }
        });
    }

    @Override
    protected void handleSubmitSm(ChannelHandlerContext ctx, SubmitSm submitSm) {
        executor.execute(() -> {
            try {
                session.incrementSubmitSmReceived();

                log.debug("Processing submit_sm: src={} dst={}",
                         submitSm.sourceAddress(), submitSm.destAddress());

                SubmitSmResult result = handler.handleSubmitSm(session, submitSm);

                SubmitSmResp response = new SubmitSmResp(
                    submitSm.sequenceNumber(),
                    result.status(),
                    result.messageId() != null ? result.messageId() : "",
                    List.of()
                );

                ctx.writeAndFlush(response);

                if (!result.isSuccess()) {
                    session.incrementErrors();
                }
            } catch (Exception e) {
                log.error("Error processing submit_sm", e);
                session.incrementErrors();

                SubmitSmResp response = new SubmitSmResp(
                    submitSm.sequenceNumber(),
                    CommandStatus.ESME_RSYSERR,
                    "",
                    List.of()
                );
                ctx.writeAndFlush(response);
            }
        });
    }

    @Override
    protected void handleDeliverSmResp(ChannelHandlerContext ctx, DeliverSmResp resp) {
        executor.execute(() -> {
            try {
                handler.handleDeliverSmResp(session, resp);
            } catch (Exception e) {
                log.error("Error processing deliver_sm_resp", e);
            }
        });
    }

    @Override
    protected void handleDataSm(ChannelHandlerContext ctx, DataSm dataSm) {
        executor.execute(() -> {
            try {
                DataSmResult result = handler.handleDataSm(session, dataSm);

                DataSmResp response = new DataSmResp(
                    dataSm.sequenceNumber(),
                    result.status(),
                    result.messageId() != null ? result.messageId() : "",
                    List.of()
                );

                ctx.writeAndFlush(response);
            } catch (Exception e) {
                log.error("Error processing data_sm", e);
                session.incrementErrors();

                DataSmResp response = new DataSmResp(
                    dataSm.sequenceNumber(),
                    CommandStatus.ESME_RSYSERR,
                    "",
                    List.of()
                );
                ctx.writeAndFlush(response);
            }
        });
    }

    @Override
    protected void handleQuerySm(ChannelHandlerContext ctx, QuerySm querySm) {
        executor.execute(() -> {
            try {
                QuerySmResult result = handler.handleQuerySm(session, querySm);

                QuerySmResp response = new QuerySmResp(
                    querySm.sequenceNumber(),
                    result.status(),
                    result.messageId() != null ? result.messageId() : "",
                    result.finalDate() != null ? result.finalDate() : "",
                    result.messageState(),
                    result.errorCode(),
                    List.of()
                );

                ctx.writeAndFlush(response);
            } catch (Exception e) {
                log.error("Error processing query_sm", e);
                session.incrementErrors();

                QuerySmResp response = new QuerySmResp(
                    querySm.sequenceNumber(),
                    CommandStatus.ESME_RSYSERR,
                    "", "", (byte) 0, (byte) 0,
                    List.of()
                );
                ctx.writeAndFlush(response);
            }
        });
    }

    @Override
    protected void handleCancelSm(ChannelHandlerContext ctx, CancelSm cancelSm) {
        executor.execute(() -> {
            try {
                CommandStatus result = handler.handleCancelSm(session, cancelSm);
                ctx.writeAndFlush(new CancelSmResp(cancelSm.sequenceNumber(), result));
            } catch (Exception e) {
                log.error("Error processing cancel_sm", e);
                session.incrementErrors();
                ctx.writeAndFlush(new CancelSmResp(cancelSm.sequenceNumber(), CommandStatus.ESME_RSYSERR));
            }
        });
    }

    @Override
    protected void handleReplaceSm(ChannelHandlerContext ctx, ReplaceSm replaceSm) {
        executor.execute(() -> {
            try {
                CommandStatus result = handler.handleReplaceSm(session, replaceSm);
                ctx.writeAndFlush(new ReplaceSmResp(replaceSm.sequenceNumber(), result));
            } catch (Exception e) {
                log.error("Error processing replace_sm", e);
                session.incrementErrors();
                ctx.writeAndFlush(new ReplaceSmResp(replaceSm.sequenceNumber(), CommandStatus.ESME_RSYSERR));
            }
        });
    }

    @Override
    protected void handleSubmitMulti(ChannelHandlerContext ctx, SubmitMulti submitMulti) {
        executor.execute(() -> {
            try {
                SubmitMultiResult result = handler.handleSubmitMulti(session, submitMulti);

                SubmitMultiResp response = new SubmitMultiResp(
                    submitMulti.sequenceNumber(),
                    result.status(),
                    result.messageId() != null ? result.messageId() : "",
                    result.failures() != null ? result.failures() : List.of(),
                    List.of()
                );

                ctx.writeAndFlush(response);
            } catch (Exception e) {
                log.error("Error processing submit_multi", e);
                session.incrementErrors();

                SubmitMultiResp response = new SubmitMultiResp(
                    submitMulti.sequenceNumber(),
                    CommandStatus.ESME_RSYSERR,
                    "", List.of(), List.of()
                );
                ctx.writeAndFlush(response);
            }
        });
    }

    /**
     * Returns the session for this handler.
     */
    public SmppServerSession getSession() {
        return session;
    }
}
