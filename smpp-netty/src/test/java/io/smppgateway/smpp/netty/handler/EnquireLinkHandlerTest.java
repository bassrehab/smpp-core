package io.smppgateway.smpp.netty.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.smppgateway.smpp.pdu.EnquireLink;
import io.smppgateway.smpp.pdu.EnquireLinkResp;
import io.smppgateway.smpp.pdu.SubmitSm;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EnquireLinkHandler Tests")
class EnquireLinkHandlerTest {

    private EmbeddedChannel channel;
    private AtomicInteger sequenceNumber;
    private EnquireLinkHandler handler;

    @BeforeEach
    void setUp() {
        sequenceNumber = new AtomicInteger(1);
        handler = new EnquireLinkHandler(sequenceNumber, 3);
        channel = new EmbeddedChannel(handler);
    }

    @Test
    @DisplayName("should respond to enquire_link with enquire_link_resp")
    void shouldRespondToEnquireLinkWithResponse() {
        EnquireLink request = new EnquireLink(123);

        channel.writeInbound(request);

        EnquireLinkResp response = channel.readOutbound();
        assertThat(response).isNotNull();
        assertThat(response.sequenceNumber()).isEqualTo(123);
        assertThat(response.commandStatus()).isEqualTo(CommandStatus.ESME_ROK);
    }

    @Test
    @DisplayName("should decrement pending count on enquire_link_resp")
    void shouldDecrementPendingCountOnResponse() {
        // Simulate sending enquire_link (would increment pending)
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);

        assertThat(handler.getPendingCount()).isEqualTo(1);

        // Receive response
        EnquireLinkResp response = new EnquireLinkResp(1, CommandStatus.ESME_ROK);
        channel.writeInbound(response);

        assertThat(handler.getPendingCount()).isZero();
    }

    @Test
    @DisplayName("should send enquire_link on write idle")
    void shouldSendEnquireLinkOnWriteIdle() {
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);

        EnquireLink sent = channel.readOutbound();
        assertThat(sent).isNotNull();
        assertThat(sent.sequenceNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("should close connection on read idle")
    void shouldCloseConnectionOnReadIdle() {
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.READER_IDLE_STATE_EVENT);

        assertThat(channel.isOpen()).isFalse();
    }

    @Test
    @DisplayName("should close connection when max pending links reached")
    void shouldCloseConnectionWhenMaxPendingLinksReached() {
        // Send 3 enquire_links
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);

        assertThat(handler.getPendingCount()).isEqualTo(3);

        // Fourth attempt should close
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);

        assertThat(channel.isOpen()).isFalse();
    }

    @Test
    @DisplayName("should pass through non-enquire_link PDUs")
    void shouldPassThroughNonEnquireLinkPdus() {
        SubmitSm submitSm = SubmitSm.builder()
                .sequenceNumber(1)
                .sourceAddress(Address.international("+1234"))
                .destAddress(Address.international("+5678"))
                .build();

        channel.writeInbound(submitSm);

        // The PDU should be passed through (no handler consumes it)
        SubmitSm passedThrough = channel.readInbound();
        assertThat(passedThrough).isEqualTo(submitSm);
    }

    @Test
    @DisplayName("should increment sequence number for each enquire_link")
    void shouldIncrementSequenceNumberForEachEnquireLink() {
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);
        EnquireLink first = channel.readOutbound();

        channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);
        EnquireLink second = channel.readOutbound();

        assertThat(first.sequenceNumber()).isEqualTo(1);
        assertThat(second.sequenceNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("should track last enquire_link time")
    void shouldTrackLastEnquireLinkTime() {
        assertThat(handler.getLastEnquireLinkTime()).isZero();

        long before = System.currentTimeMillis();
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);
        long after = System.currentTimeMillis();

        assertThat(handler.getLastEnquireLinkTime()).isBetween(before, after);
    }

    @Test
    @DisplayName("should create handler with default max pending links")
    void shouldCreateHandlerWithDefaultMaxPendingLinks() {
        EnquireLinkHandler defaultHandler = new EnquireLinkHandler(new AtomicInteger(1));
        EmbeddedChannel ch = new EmbeddedChannel(defaultHandler);

        // Default is 3, so we can send 3 without close
        ch.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);
        ch.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);
        ch.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);

        assertThat(ch.isOpen()).isTrue();

        ch.pipeline().fireUserEventTriggered(IdleStateEvent.WRITER_IDLE_STATE_EVENT);
        assertThat(ch.isOpen()).isFalse();
    }

    @Test
    @DisplayName("should handle ALL_IDLE state without action")
    void shouldHandleAllIdleStateWithoutAction() {
        channel.pipeline().fireUserEventTriggered(IdleStateEvent.ALL_IDLE_STATE_EVENT);

        assertThat(channel.isOpen()).isTrue();
        assertThat((EnquireLink) channel.readOutbound()).isNull();
    }
}
