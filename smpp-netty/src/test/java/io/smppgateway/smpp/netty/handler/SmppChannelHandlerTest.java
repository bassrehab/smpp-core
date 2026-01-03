package io.smppgateway.smpp.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.state.SessionState;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandStatus;
import io.smppgateway.smpp.types.SmppBindType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppChannelHandler Tests")
class SmppChannelHandlerTest {

    private TestableSmppChannelHandler handler;
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        handler = new TestableSmppChannelHandler();
        channel = new EmbeddedChannel(handler);
    }

    @Test
    @DisplayName("should transition to OPEN state on channel active")
    void shouldTransitionToOpenStateOnChannelActive() {
        // Channel is already active from EmbeddedChannel constructor
        assertThat(handler.getSessionState()).isEqualTo(SessionState.OPEN);
    }

    @Test
    @DisplayName("should transition to CLOSED state on channel inactive")
    void shouldTransitionToClosedStateOnChannelInactive() {
        channel.close();

        assertThat(handler.getSessionState()).isEqualTo(SessionState.CLOSED);
    }

    @Test
    @DisplayName("should respond to unbind with unbind_resp and close")
    void shouldRespondToUnbindAndClose() {
        Unbind unbind = new Unbind(123);
        channel.writeInbound(unbind);

        UnbindResp response = channel.readOutbound();
        assertThat(response).isNotNull();
        assertThat(response.sequenceNumber()).isEqualTo(123);
        assertThat(response.commandStatus()).isEqualTo(CommandStatus.ESME_ROK);
        assertThat(channel.isOpen()).isFalse();
    }

    @Test
    @DisplayName("should respond to enquire_link with enquire_link_resp")
    void shouldRespondToEnquireLinkWithResponse() {
        EnquireLink link = new EnquireLink(456);
        channel.writeInbound(link);

        EnquireLinkResp response = channel.readOutbound();
        assertThat(response).isNotNull();
        assertThat(response.sequenceNumber()).isEqualTo(456);
        assertThat(response.commandStatus()).isEqualTo(CommandStatus.ESME_ROK);
    }

    @Test
    @DisplayName("should close connection on unbind_resp")
    void shouldCloseConnectionOnUnbindResp() {
        UnbindResp resp = new UnbindResp(789, CommandStatus.ESME_ROK);
        channel.writeInbound(resp);

        assertThat(channel.isOpen()).isFalse();
    }

    @Test
    @DisplayName("should close connection on read idle")
    void shouldCloseConnectionOnReadIdle() {
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.READER_IDLE_STATE_EVENT);

        assertThat(channel.isOpen()).isFalse();
    }

    @Test
    @DisplayName("should send enquire_link on write idle when bound")
    void shouldSendEnquireLinkOnWriteIdleWhenBound() {
        // Simulate binding
        handler.simulateBind(SmppBindType.TRANSCEIVER);

        channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);

        EnquireLink sent = channel.readOutbound();
        assertThat(sent).isNotNull();
    }

    @Test
    @DisplayName("should not send enquire_link on write idle when not bound")
    void shouldNotSendEnquireLinkOnWriteIdleWhenNotBound() {
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);

        EnquireLink sent = channel.readOutbound();
        assertThat(sent).isNull();
    }

    @Test
    @DisplayName("should generate incrementing sequence numbers")
    void shouldGenerateIncrementingSequenceNumbers() {
        int first = handler.getNextSequenceNumber();
        int second = handler.getNextSequenceNumber();
        int third = handler.getNextSequenceNumber();

        assertThat(first).isEqualTo(1);
        assertThat(second).isEqualTo(2);
        assertThat(third).isEqualTo(3);
    }

    @Test
    @DisplayName("should wrap sequence number at max value")
    void shouldWrapSequenceNumberAtMaxValue() {
        handler.setSequenceNumber(0x7FFFFFFF);

        int maxVal = handler.getNextSequenceNumber();
        int wrapped = handler.getNextSequenceNumber();

        assertThat(maxVal).isEqualTo(0x7FFFFFFF);
        assertThat(wrapped).isEqualTo(1);
    }

    @Test
    @DisplayName("should route submit_sm to handler method")
    void shouldRouteSubmitSmToHandlerMethod() {
        SubmitSm submitSm = SubmitSm.builder()
                .sequenceNumber(1)
                .sourceAddress(Address.international("+1234"))
                .destAddress(Address.international("+5678"))
                .build();

        channel.writeInbound(submitSm);

        assertThat(handler.lastSubmitSm).isEqualTo(submitSm);
    }

    @Test
    @DisplayName("should route deliver_sm to handler method")
    void shouldRouteDeliverSmToHandlerMethod() {
        DeliverSm deliverSm = DeliverSm.builder()
                .sequenceNumber(2)
                .sourceAddress(Address.international("+1234"))
                .destAddress(Address.international("+5678"))
                .build();

        channel.writeInbound(deliverSm);

        assertThat(handler.lastDeliverSm).isEqualTo(deliverSm);
    }

    @Test
    @DisplayName("should route generic_nack to handler method")
    void shouldRouteGenericNackToHandlerMethod() {
        GenericNack nack = new GenericNack(1, CommandStatus.ESME_RINVCMDID);

        channel.writeInbound(nack);

        assertThat(handler.lastGenericNack).isEqualTo(nack);
    }

    @Test
    @DisplayName("should return isBound false initially")
    void shouldReturnIsBoundFalseInitially() {
        assertThat(handler.isBound()).isFalse();
    }

    @Test
    @DisplayName("should return isBound true after bind")
    void shouldReturnIsBoundTrueAfterBind() {
        handler.simulateBind(SmppBindType.TRANSCEIVER);

        assertThat(handler.isBound()).isTrue();
    }

    @Test
    @DisplayName("should send generic nack")
    void shouldSendGenericNack() {
        handler.doSendGenericNack(123, CommandStatus.ESME_RINVCMDID);

        GenericNack nack = channel.readOutbound();
        assertThat(nack).isNotNull();
        assertThat(nack.sequenceNumber()).isEqualTo(123);
        assertThat(nack.commandStatus()).isEqualTo(CommandStatus.ESME_RINVCMDID);
    }

    @Test
    @DisplayName("should handle bind transceiver response success")
    void shouldHandleBindTransceiverResponseSuccess() {
        BindTransceiverResp resp = new BindTransceiverResp(1, CommandStatus.ESME_ROK, "SMSC", java.util.List.of());

        channel.writeInbound(resp);

        assertThat(handler.isBound()).isTrue();
        assertThat(handler.getSessionState()).isEqualTo(SessionState.BOUND_TRX);
    }

    @Test
    @DisplayName("should not bind on failed bind response")
    void shouldNotBindOnFailedBindResponse() {
        BindTransceiverResp resp = new BindTransceiverResp(1, CommandStatus.ESME_RBINDFAIL, "", java.util.List.of());

        channel.writeInbound(resp);

        assertThat(handler.isBound()).isFalse();
    }

    /**
     * Testable implementation of SmppChannelHandler
     */
    static class TestableSmppChannelHandler extends SmppChannelHandler {
        SubmitSm lastSubmitSm;
        DeliverSm lastDeliverSm;
        GenericNack lastGenericNack;

        @Override
        protected void handleSubmitSm(ChannelHandlerContext ctx, SubmitSm sm) {
            super.handleSubmitSm(ctx, sm);
            this.lastSubmitSm = sm;
        }

        @Override
        protected void handleDeliverSm(ChannelHandlerContext ctx, DeliverSm sm) {
            super.handleDeliverSm(ctx, sm);
            this.lastDeliverSm = sm;
        }

        @Override
        protected void handleGenericNack(ChannelHandlerContext ctx, GenericNack nack) {
            super.handleGenericNack(ctx, nack);
            this.lastGenericNack = nack;
        }

        public void simulateBind(SmppBindType bindType) {
            stateMachine.onBind(bindType);
        }

        public int getNextSequenceNumber() {
            return nextSequenceNumber();
        }

        public void setSequenceNumber(int seq) {
            sequenceNumber.set(seq);
        }

        public void doSendGenericNack(int seq, CommandStatus status) {
            sendGenericNack(ctx, seq, status);
        }
    }
}
