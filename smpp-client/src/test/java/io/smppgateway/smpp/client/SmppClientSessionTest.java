package io.smppgateway.smpp.client;

import io.netty.channel.embedded.EmbeddedChannel;
import io.smppgateway.smpp.exception.SmppException;
import io.smppgateway.smpp.netty.window.Window;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.state.SessionState;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandStatus;
import io.smppgateway.smpp.types.SmppBindType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppClientSession Tests")
class SmppClientSessionTest {

    private EmbeddedChannel channel;
    private Window<PduResponse> window;
    private SmppClientConfiguration config;
    private SmppClientSession session;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel();
        window = new Window<>(100, Duration.ofSeconds(30));
        config = SmppClientConfiguration.builder()
                .host("localhost")
                .port(2775)
                .systemId("test")
                .password("test")
                .build();
        session = new SmppClientSession(channel, window, config);
    }

    @Nested
    @DisplayName("Identity Tests")
    class IdentityTests {

        @Test
        @DisplayName("should have unique session ID")
        void shouldHaveUniqueSessionId() {
            assertThat(session.getSessionId()).isNotNull();
            assertThat(session.getSessionId()).isNotEmpty();

            SmppClientSession otherSession = new SmppClientSession(channel, window, config);
            assertThat(session.getSessionId()).isNotEqualTo(otherSession.getSessionId());
        }

        @Test
        @DisplayName("should return null server system ID when not bound")
        void shouldReturnNullServerSystemIdWhenNotBound() {
            assertThat(session.getServerSystemId()).isNull();
        }

        @Test
        @DisplayName("should return creation time")
        void shouldReturnCreationTime() {
            Instant before = Instant.now();
            SmppClientSession newSession = new SmppClientSession(channel, window, config);
            Instant after = Instant.now();

            assertThat(newSession.getCreatedAt()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("State Tests")
    class StateTests {

        @Test
        @DisplayName("should start in OPEN state")
        void shouldStartInOpenState() {
            assertThat(session.getState()).isEqualTo(SessionState.OPEN);
        }

        @Test
        @DisplayName("should not be bound initially")
        void shouldNotBeBoundInitially() {
            assertThat(session.isBound()).isFalse();
            assertThat(session.getBindType()).isNull();
        }

        @Test
        @DisplayName("should transition to BOUND_TX for transmitter")
        void shouldTransitionToBoundTxForTransmitter() {
            session.setBound(SmppBindType.TRANSMITTER, "SMSC");

            assertThat(session.isBound()).isTrue();
            assertThat(session.getState()).isEqualTo(SessionState.BOUND_TX);
            assertThat(session.getBindType()).isEqualTo(SmppBindType.TRANSMITTER);
            assertThat(session.getServerSystemId()).isEqualTo("SMSC");
        }

        @Test
        @DisplayName("should transition to BOUND_RX for receiver")
        void shouldTransitionToBoundRxForReceiver() {
            session.setBound(SmppBindType.RECEIVER, "SMSC");

            assertThat(session.isBound()).isTrue();
            assertThat(session.getState()).isEqualTo(SessionState.BOUND_RX);
            assertThat(session.getBindType()).isEqualTo(SmppBindType.RECEIVER);
        }

        @Test
        @DisplayName("should transition to BOUND_TRX for transceiver")
        void shouldTransitionToBoundTrxForTransceiver() {
            session.setBound(SmppBindType.TRANSCEIVER, "SMSC");

            assertThat(session.isBound()).isTrue();
            assertThat(session.getState()).isEqualTo(SessionState.BOUND_TRX);
            assertThat(session.getBindType()).isEqualTo(SmppBindType.TRANSCEIVER);
        }

        @Test
        @DisplayName("should return isActive based on channel")
        void shouldReturnIsActiveBasedOnChannel() {
            assertThat(session.isActive()).isTrue();
            channel.close();
            assertThat(session.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Submit Tests")
    class SubmitTests {

        @Test
        @DisplayName("should throw exception when submitting without being bound")
        void shouldThrowExceptionWhenSubmittingWithoutBeingBound() {
            SubmitSm submitSm = SubmitSm.builder()
                    .sequenceNumber(1)
                    .sourceAddress(Address.international("+1234"))
                    .destAddress(Address.international("+5678"))
                    .build();

            assertThatThrownBy(() -> session.submitSmAsync(submitSm))
                    .isInstanceOf(SmppException.class)
                    .hasMessageContaining("not bound");
        }
    }

    @Nested
    @DisplayName("Sequence Number Tests")
    class SequenceNumberTests {

        @Test
        @DisplayName("should increment sequence numbers")
        void shouldIncrementSequenceNumbers() {
            int first = session.nextSequenceNumber();
            int second = session.nextSequenceNumber();
            int third = session.nextSequenceNumber();

            assertThat(first).isEqualTo(1);
            assertThat(second).isEqualTo(2);
            assertThat(third).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Attributes Tests")
    class AttributesTests {

        @Test
        @DisplayName("should set and get attributes")
        void shouldSetAndGetAttributes() {
            session.setAttribute("key", "value");

            String value = session.getAttribute("key");
            assertThat(value).isEqualTo("value");
        }

        @Test
        @DisplayName("should return null for missing attribute")
        void shouldReturnNullForMissingAttribute() {
            String value = session.getAttribute("missing");
            assertThat(value).isNull();
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("should start with zero statistics")
        void shouldStartWithZeroStatistics() {
            assertThat(session.getSubmitSmSent()).isZero();
            assertThat(session.getDeliverSmReceived()).isZero();
            assertThat(session.getErrors()).isZero();
        }

        @Test
        @DisplayName("should increment deliver_sm received counter")
        void shouldIncrementDeliverSmReceivedCounter() {
            session.incrementDeliverSmReceived();
            session.incrementDeliverSmReceived();

            assertThat(session.getDeliverSmReceived()).isEqualTo(2);
        }

        @Test
        @DisplayName("should increment error counter")
        void shouldIncrementErrorCounter() {
            session.incrementErrors();

            assertThat(session.getErrors()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Window Tests")
    class WindowTests {

        @Test
        @DisplayName("should return pending count")
        void shouldReturnPendingCount() {
            assertThat(session.getPendingCount()).isZero();
        }

        @Test
        @DisplayName("should return available slots")
        void shouldReturnAvailableSlots() {
            assertThat(session.getAvailableSlots()).isEqualTo(100);
        }

        @Test
        @DisplayName("should complete request")
        void shouldCompleteRequest() {
            // Just verify the method doesn't throw
            boolean result = session.completeRequest(1, new SubmitSmResp(1, CommandStatus.ESME_ROK, "MSG001", java.util.List.of()));
            assertThat(result).isFalse(); // No pending request with sequence 1
        }
    }

    @Nested
    @DisplayName("Unbind and Close Tests")
    class UnbindAndCloseTests {

        @Test
        @DisplayName("should send unbind and transition to CLOSED")
        void shouldSendUnbindAndTransitionToClosed() {
            session.setBound(SmppBindType.TRANSCEIVER, "SMSC");
            session.unbind();

            assertThat(session.getState()).isEqualTo(SessionState.CLOSED);
            Unbind sent = channel.readOutbound();
            assertThat(sent).isNotNull();
        }

        @Test
        @DisplayName("should close channel and window")
        void shouldCloseChannelAndWindow() {
            session.close();

            assertThat(channel.isOpen()).isFalse();
        }
    }

    @Nested
    @DisplayName("Channel Tests")
    class ChannelTests {

        @Test
        @DisplayName("should return channel")
        void shouldReturnChannel() {
            assertThat(session.getChannel()).isEqualTo(channel);
        }
    }

    @Test
    @DisplayName("should get session id and state")
    void shouldGetSessionIdAndState() {
        session.setBound(SmppBindType.TRANSCEIVER, "TestSMSC");

        assertThat(session.getSessionId()).isNotNull();
        assertThat(session.getServerSystemId()).isEqualTo("TestSMSC");
        assertThat(session.getState()).isEqualTo(SessionState.BOUND_TRX);
    }
}
