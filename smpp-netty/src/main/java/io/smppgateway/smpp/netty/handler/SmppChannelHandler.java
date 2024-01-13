package io.smppgateway.smpp.netty.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.state.SessionState;
import io.smppgateway.smpp.state.SessionStateMachine;
import io.smppgateway.smpp.types.CommandStatus;
import io.smppgateway.smpp.types.SmppBindType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base SMPP channel handler providing common session management functionality.
 *
 * <p>This handler manages:
 * <ul>
 *   <li>Session state machine transitions</li>
 *   <li>Sequence number generation</li>
 *   <li>Idle state handling (keepalive)</li>
 *   <li>Generic NACK responses for invalid states</li>
 * </ul>
 *
 * <p>Subclasses should override the PDU handling methods to implement
 * application-specific logic.
 */
public abstract class SmppChannelHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(SmppChannelHandler.class);

    protected final SessionStateMachine stateMachine = new SessionStateMachine();
    protected final AtomicInteger sequenceNumber = new AtomicInteger(1);

    protected ChannelHandlerContext ctx;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.handlerAdded(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel active: {}", ctx.channel().remoteAddress());
        stateMachine.onConnect();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel inactive: {}", ctx.channel().remoteAddress());
        stateMachine.onClose();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Pdu pdu) {
            try {
                handlePdu(ctx, pdu);
            } catch (Exception e) {
                log.error("Error handling PDU: {}", pdu.commandId(), e);
                exceptionCaught(ctx, e);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleEvent) {
            handleIdleState(ctx, idleEvent.state());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Channel exception: {}", cause.getMessage(), cause);
        ctx.close();
    }

    /**
     * Handles idle state events. Override to customize behavior.
     */
    protected void handleIdleState(ChannelHandlerContext ctx, IdleState state) {
        switch (state) {
            case READER_IDLE -> {
                log.debug("Read idle, closing channel");
                ctx.close();
            }
            case WRITER_IDLE -> {
                if (stateMachine.isBound()) {
                    log.debug("Write idle, sending enquire_link");
                    sendEnquireLink(ctx);
                }
            }
            case ALL_IDLE -> { /* Not used */ }
        }
    }

    /**
     * Routes the PDU to the appropriate handler method.
     */
    protected void handlePdu(ChannelHandlerContext ctx, Pdu pdu) {
        switch (pdu) {
            case BindReceiver bind -> handleBind(ctx, bind, SmppBindType.RECEIVER);
            case BindTransmitter bind -> handleBind(ctx, bind, SmppBindType.TRANSMITTER);
            case BindTransceiver bind -> handleBind(ctx, bind, SmppBindType.TRANSCEIVER);
            case BindReceiverResp resp -> handleBindResponse(ctx, resp);
            case BindTransmitterResp resp -> handleBindResponse(ctx, resp);
            case BindTransceiverResp resp -> handleBindResponse(ctx, resp);
            case Unbind unbind -> handleUnbind(ctx, unbind);
            case UnbindResp resp -> handleUnbindResponse(ctx, resp);
            case SubmitSm sm -> handleSubmitSm(ctx, sm);
            case SubmitSmResp resp -> handleSubmitSmResp(ctx, resp);
            case DeliverSm sm -> handleDeliverSm(ctx, sm);
            case DeliverSmResp resp -> handleDeliverSmResp(ctx, resp);
            case EnquireLink link -> handleEnquireLink(ctx, link);
            case EnquireLinkResp resp -> handleEnquireLinkResp(ctx, resp);
            case GenericNack nack -> handleGenericNack(ctx, nack);
            case QuerySm query -> handleQuerySm(ctx, query);
            case QuerySmResp resp -> handleQuerySmResp(ctx, resp);
            case CancelSm cancel -> handleCancelSm(ctx, cancel);
            case CancelSmResp resp -> handleCancelSmResp(ctx, resp);
            case ReplaceSm replace -> handleReplaceSm(ctx, replace);
            case ReplaceSmResp resp -> handleReplaceSmResp(ctx, resp);
            case DataSm data -> handleDataSm(ctx, data);
            case DataSmResp resp -> handleDataSmResp(ctx, resp);
            case Outbind outbind -> handleOutbind(ctx, outbind);
            case AlertNotification alert -> handleAlertNotification(ctx, alert);
            case SubmitMulti multi -> handleSubmitMulti(ctx, multi);
            case SubmitMultiResp resp -> handleSubmitMultiResp(ctx, resp);
        }
    }

    // Bind handling

    protected void handleBind(ChannelHandlerContext ctx, PduRequest<?> bindRequest, SmppBindType bindType) {
        log.debug("Received bind request: {} type={}", bindRequest.commandId(), bindType);
        // Subclass should implement authentication and send response
    }

    protected void handleBindResponse(ChannelHandlerContext ctx, PduResponse bindResponse) {
        log.debug("Received bind response: {} status={}", bindResponse.commandId(), bindResponse.commandStatus());
        if (bindResponse.commandStatus() == CommandStatus.ESME_ROK) {
            SmppBindType bindType = switch (bindResponse) {
                case BindReceiverResp ignored -> SmppBindType.RECEIVER;
                case BindTransmitterResp ignored -> SmppBindType.TRANSMITTER;
                case BindTransceiverResp ignored -> SmppBindType.TRANSCEIVER;
                default -> null;
            };
            if (bindType != null) {
                stateMachine.onBind(bindType);
            }
        }
    }

    // Unbind handling

    protected void handleUnbind(ChannelHandlerContext ctx, Unbind unbind) {
        log.debug("Received unbind request");
        ctx.writeAndFlush(new UnbindResp(unbind.sequenceNumber(), CommandStatus.ESME_ROK));
        stateMachine.onUnbind();
        ctx.close();
    }

    protected void handleUnbindResponse(ChannelHandlerContext ctx, UnbindResp resp) {
        log.debug("Received unbind response: status={}", resp.commandStatus());
        stateMachine.onUnbind();
        ctx.close();
    }

    // Enquire link handling

    protected void handleEnquireLink(ChannelHandlerContext ctx, EnquireLink link) {
        log.trace("Received enquire_link, sending response");
        ctx.writeAndFlush(new EnquireLinkResp(link.sequenceNumber(), CommandStatus.ESME_ROK));
    }

    protected void handleEnquireLinkResp(ChannelHandlerContext ctx, EnquireLinkResp resp) {
        log.trace("Received enquire_link_resp: status={}", resp.commandStatus());
        // Response received, connection is alive
    }

    protected void sendEnquireLink(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new EnquireLink(nextSequenceNumber()));
    }

    // Message handling - subclasses should override

    protected void handleSubmitSm(ChannelHandlerContext ctx, SubmitSm sm) {
        log.debug("Received submit_sm from {} to {}", sm.sourceAddress(), sm.destAddress());
    }

    protected void handleSubmitSmResp(ChannelHandlerContext ctx, SubmitSmResp resp) {
        log.debug("Received submit_sm_resp: messageId={}", resp.messageId());
    }

    protected void handleDeliverSm(ChannelHandlerContext ctx, DeliverSm sm) {
        log.debug("Received deliver_sm from {} to {}", sm.sourceAddress(), sm.destAddress());
    }

    protected void handleDeliverSmResp(ChannelHandlerContext ctx, DeliverSmResp resp) {
        log.debug("Received deliver_sm_resp: messageId={}", resp.messageId());
    }

    protected void handleGenericNack(ChannelHandlerContext ctx, GenericNack nack) {
        log.warn("Received generic_nack: status={}", nack.commandStatus());
    }

    protected void handleQuerySm(ChannelHandlerContext ctx, QuerySm query) {
        log.debug("Received query_sm: messageId={}", query.messageId());
    }

    protected void handleQuerySmResp(ChannelHandlerContext ctx, QuerySmResp resp) {
        log.debug("Received query_sm_resp: messageId={}", resp.messageId());
    }

    protected void handleCancelSm(ChannelHandlerContext ctx, CancelSm cancel) {
        log.debug("Received cancel_sm: messageId={}", cancel.messageId());
    }

    protected void handleCancelSmResp(ChannelHandlerContext ctx, CancelSmResp resp) {
        log.debug("Received cancel_sm_resp: status={}", resp.commandStatus());
    }

    protected void handleReplaceSm(ChannelHandlerContext ctx, ReplaceSm replace) {
        log.debug("Received replace_sm: messageId={}", replace.messageId());
    }

    protected void handleReplaceSmResp(ChannelHandlerContext ctx, ReplaceSmResp resp) {
        log.debug("Received replace_sm_resp: status={}", resp.commandStatus());
    }

    protected void handleDataSm(ChannelHandlerContext ctx, DataSm data) {
        log.debug("Received data_sm from {} to {}", data.sourceAddress(), data.destAddress());
    }

    protected void handleDataSmResp(ChannelHandlerContext ctx, DataSmResp resp) {
        log.debug("Received data_sm_resp: messageId={}", resp.messageId());
    }

    protected void handleOutbind(ChannelHandlerContext ctx, Outbind outbind) {
        log.debug("Received outbind: systemId={}", outbind.systemId());
    }

    protected void handleAlertNotification(ChannelHandlerContext ctx, AlertNotification alert) {
        log.debug("Received alert_notification from {}", alert.sourceAddress());
    }

    protected void handleSubmitMulti(ChannelHandlerContext ctx, SubmitMulti multi) {
        log.debug("Received submit_multi from {} to {} destinations",
                  multi.sourceAddress(), multi.destAddresses().size());
    }

    protected void handleSubmitMultiResp(ChannelHandlerContext ctx, SubmitMultiResp resp) {
        log.debug("Received submit_multi_resp: messageId={}", resp.messageId());
    }

    // Utility methods

    /**
     * Returns the next sequence number for outgoing PDUs.
     */
    protected int nextSequenceNumber() {
        return sequenceNumber.getAndUpdate(n -> (n >= 0x7FFFFFFF) ? 1 : n + 1);
    }

    /**
     * Returns the current session state.
     */
    public SessionState getSessionState() {
        return stateMachine.getState();
    }

    /**
     * Returns true if the session is bound.
     */
    public boolean isBound() {
        return stateMachine.isBound();
    }

    /**
     * Sends a PDU asynchronously.
     */
    public ChannelFuture send(Pdu pdu) {
        return ctx.writeAndFlush(pdu);
    }

    /**
     * Sends a PDU and waits for completion.
     */
    public ChannelFuture sendSync(Pdu pdu) {
        return ctx.writeAndFlush(pdu).syncUninterruptibly();
    }

    /**
     * Sends a generic NACK for an invalid PDU.
     */
    protected void sendGenericNack(ChannelHandlerContext ctx, int sequenceNumber, CommandStatus status) {
        ctx.writeAndFlush(new GenericNack(sequenceNumber, status));
    }
}
