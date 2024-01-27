package io.smppgateway.smpp.client.impl;

import io.netty.channel.ChannelHandlerContext;
import io.smppgateway.smpp.client.SmppClientHandler;
import io.smppgateway.smpp.client.SmppClientHandler.*;
import io.smppgateway.smpp.client.SmppClientSession;
import io.smppgateway.smpp.netty.handler.SmppChannelHandler;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.types.CommandStatus;
import io.smppgateway.smpp.types.SmppBindType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Netty channel handler for client-side SMPP sessions.
 */
public class SmppClientSessionHandler extends SmppChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(SmppClientSessionHandler.class);

    private final SmppClientHandler handler;
    private final Executor executor;
    private final Consumer<Throwable> onDisconnect;

    private SmppClientSession session;

    public SmppClientSessionHandler(SmppClientHandler handler, Executor executor,
                                    Consumer<Throwable> onDisconnect) {
        this.handler = handler;
        this.executor = executor;
        this.onDisconnect = onDisconnect;
    }

    public void setSession(SmppClientSession session) {
        this.session = session;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        if (session != null) {
            executor.execute(() -> {
                try {
                    handler.sessionUnbound(session);
                } catch (Exception e) {
                    log.error("Error in sessionUnbound handler", e);
                }
            });
        }

        onDisconnect.accept(new Exception("Connection closed"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Channel exception", cause);

        if (session != null) {
            session.incrementErrors();
        }

        onDisconnect.accept(cause);
        ctx.close();
    }

    @Override
    protected void handleBindResponse(ChannelHandlerContext ctx, PduResponse bindResponse) {
        log.debug("Received bind response: status={}", bindResponse.commandStatus());
        super.handleBindResponse(ctx, bindResponse);
    }

    @Override
    protected void handleDeliverSm(ChannelHandlerContext ctx, DeliverSm deliverSm) {
        executor.execute(() -> {
            try {
                if (session != null) {
                    session.incrementDeliverSmReceived();
                }

                log.debug("Processing deliver_sm: src={} dst={}",
                         deliverSm.sourceAddress(), deliverSm.destAddress());

                DeliverSmResult result = handler.handleDeliverSm(session, deliverSm);

                DeliverSmResp response = new DeliverSmResp(
                    deliverSm.sequenceNumber(),
                    result.status(),
                    result.messageId(),
                    List.of()
                );

                ctx.writeAndFlush(response);
            } catch (Exception e) {
                log.error("Error processing deliver_sm", e);
                if (session != null) {
                    session.incrementErrors();
                }

                DeliverSmResp response = new DeliverSmResp(
                    deliverSm.sequenceNumber(),
                    CommandStatus.ESME_RSYSERR,
                    "",
                    List.of()
                );
                ctx.writeAndFlush(response);
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
                    result.messageId(),
                    List.of()
                );

                ctx.writeAndFlush(response);
            } catch (Exception e) {
                log.error("Error processing data_sm", e);
                if (session != null) {
                    session.incrementErrors();
                }

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
    protected void handleAlertNotification(ChannelHandlerContext ctx, AlertNotification alert) {
        executor.execute(() -> {
            try {
                handler.handleAlertNotification(session, alert);
            } catch (Exception e) {
                log.error("Error processing alert_notification", e);
            }
        });
    }

    @Override
    protected void handleSubmitSmResp(ChannelHandlerContext ctx, SubmitSmResp resp) {
        // Complete pending request
        if (session != null) {
            session.completeRequest(resp.sequenceNumber(), resp);
        }
    }

    @Override
    protected void handleDataSmResp(ChannelHandlerContext ctx, DataSmResp resp) {
        if (session != null) {
            session.completeRequest(resp.sequenceNumber(), resp);
        }
    }

    @Override
    protected void handleQuerySmResp(ChannelHandlerContext ctx, QuerySmResp resp) {
        if (session != null) {
            session.completeRequest(resp.sequenceNumber(), resp);
        }
    }

    @Override
    protected void handleCancelSmResp(ChannelHandlerContext ctx, CancelSmResp resp) {
        if (session != null) {
            session.completeRequest(resp.sequenceNumber(), resp);
        }
    }

    @Override
    protected void handleReplaceSmResp(ChannelHandlerContext ctx, ReplaceSmResp resp) {
        if (session != null) {
            session.completeRequest(resp.sequenceNumber(), resp);
        }
    }

    @Override
    protected void handleSubmitMultiResp(ChannelHandlerContext ctx, SubmitMultiResp resp) {
        if (session != null) {
            session.completeRequest(resp.sequenceNumber(), resp);
        }
    }

    public SmppClientSession getSession() {
        return session;
    }
}
