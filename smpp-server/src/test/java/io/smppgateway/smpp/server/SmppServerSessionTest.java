package io.smppgateway.smpp.server;

import io.netty.channel.embedded.EmbeddedChannel;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.state.SessionState;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.SmppBindType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppServerSession Tests")
class SmppServerSessionTest {

    private EmbeddedChannel channel;
    private SmppServerSession session;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel();
        session = new SmppServerSession(channel);
    }

    @Nested
    @DisplayName("Identity Tests")
    class IdentityTests {

        @Test
        @DisplayName("should have unique session ID")
        void shouldHaveUniqueSessionId() {
            assertThat(session.getSessionId()).isNotNull();
            assertThat(session.getSessionId()).isNotEmpty();

            SmppServerSession otherSession = new SmppServerSession(channel);
            assertThat(session.getSessionId()).isNotEqualTo(otherSession.getSessionId());
        }

        @Test
        @DisplayName("should return null system ID when not bound")
        void shouldReturnNullSystemIdWhenNotBound() {
            assertThat(session.getSystemId()).isNull();
        }

        @Test
        @DisplayName("should return creation time")
        void shouldReturnCreationTime() {
            Instant before = Instant.now();
            SmppServerSession newSession = new SmppServerSession(channel);
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
            session.setBound(SmppBindType.TRANSMITTER, "test", "password", "type", (byte) 0x34);

            assertThat(session.isBound()).isTrue();
            assertThat(session.getState()).isEqualTo(SessionState.BOUND_TX);
            assertThat(session.getBindType()).isEqualTo(SmppBindType.TRANSMITTER);
            assertThat(session.getSystemId()).isEqualTo("test");
        }

        @Test
        @DisplayName("should transition to BOUND_RX for receiver")
        void shouldTransitionToBoundRxForReceiver() {
            session.setBound(SmppBindType.RECEIVER, "test", "password", "type", (byte) 0x34);

            assertThat(session.isBound()).isTrue();
            assertThat(session.getState()).isEqualTo(SessionState.BOUND_RX);
            assertThat(session.getBindType()).isEqualTo(SmppBindType.RECEIVER);
        }

        @Test
        @DisplayName("should transition to BOUND_TRX for transceiver")
        void shouldTransitionToBoundTrxForTransceiver() {
            session.setBound(SmppBindType.TRANSCEIVER, "test", "password", "type", (byte) 0x34);

            assertThat(session.isBound()).isTrue();
            assertThat(session.getState()).isEqualTo(SessionState.BOUND_TRX);
            assertThat(session.getBindType()).isEqualTo(SmppBindType.TRANSCEIVER);
        }

        @Test
        @DisplayName("should transition to CLOSED on unbind")
        void shouldTransitionToClosedOnUnbind() {
            session.setBound(SmppBindType.TRANSCEIVER, "test", "password", "type", (byte) 0x34);
            session.setUnbound();

            assertThat(session.getState()).isEqualTo(SessionState.CLOSED);
        }
    }

    @Nested
    @DisplayName("Capability Tests")
    class CapabilityTests {

        @Test
        @DisplayName("transmitter can transmit but not receive")
        void transmitterCanTransmitButNotReceive() {
            session.setBound(SmppBindType.TRANSMITTER, "test", "pw", "type", (byte) 0x34);

            assertThat(session.canTransmit()).isTrue();
            assertThat(session.canReceive()).isFalse();
        }

        @Test
        @DisplayName("receiver can receive but not transmit")
        void receiverCanReceiveButNotTransmit() {
            session.setBound(SmppBindType.RECEIVER, "test", "pw", "type", (byte) 0x34);

            assertThat(session.canReceive()).isTrue();
            assertThat(session.canTransmit()).isFalse();
        }

        @Test
        @DisplayName("transceiver can both transmit and receive")
        void transceiverCanBothTransmitAndReceive() {
            session.setBound(SmppBindType.TRANSCEIVER, "test", "pw", "type", (byte) 0x34);

            assertThat(session.canTransmit()).isTrue();
            assertThat(session.canReceive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Sending Messages Tests")
    class SendingMessagesTests {

        @Test
        @DisplayName("should fail sendDeliverSm if not bound as receiver")
        void shouldFailSendDeliverSmIfNotBoundAsReceiver() {
            session.setBound(SmppBindType.TRANSMITTER, "test", "pw", "type", (byte) 0x34);

            DeliverSm deliverSm = DeliverSm.builder()
                    .sequenceNumber(1)
                    .sourceAddress(Address.international("+1234"))
                    .destAddress(Address.international("+5678"))
                    .build();

            CompletableFuture<DeliverSmResp> future = session.sendDeliverSm(deliverSm);

            assertThat(future).isCompletedExceptionally();
            assertThatThrownBy(future::get)
                    .hasCauseInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot receive");
        }

        @Test
        @DisplayName("should send deliver_sm when bound as receiver")
        void shouldSendDeliverSmWhenBoundAsReceiver() {
            session.setBound(SmppBindType.RECEIVER, "test", "pw", "type", (byte) 0x34);

            DeliverSm deliverSm = DeliverSm.builder()
                    .sequenceNumber(1)
                    .sourceAddress(Address.international("+1234"))
                    .destAddress(Address.international("+5678"))
                    .build();

            session.sendDeliverSm(deliverSm);

            DeliverSm sent = channel.readOutbound();
            assertThat(sent).isNotNull();
        }

        @Test
        @DisplayName("should fail sendAlertNotification if not receiver")
        void shouldFailSendAlertNotificationIfNotReceiver() {
            session.setBound(SmppBindType.TRANSMITTER, "test", "pw", "type", (byte) 0x34);

            assertThatThrownBy(() ->
                    session.sendAlertNotification(
                            Address.international("+1234"),
                            Address.international("+5678")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot receive");
        }

        @Test
        @DisplayName("should send alert_notification when receiver")
        void shouldSendAlertNotificationWhenReceiver() {
            session.setBound(SmppBindType.RECEIVER, "test", "pw", "type", (byte) 0x34);

            session.sendAlertNotification(
                    Address.international("+1234"),
                    Address.international("+5678"));

            AlertNotification sent = channel.readOutbound();
            assertThat(sent).isNotNull();
        }

        @Test
        @DisplayName("should send outbind")
        void shouldSendOutbind() {
            session.sendOutbind("SMSC", "password");

            Outbind sent = channel.readOutbound();
            assertThat(sent).isNotNull();
            assertThat(sent.systemId()).isEqualTo("SMSC");
            assertThat(sent.password()).isEqualTo("password");
        }

        @Test
        @DisplayName("should send unbind")
        void shouldSendUnbind() {
            session.unbind();

            Unbind sent = channel.readOutbound();
            assertThat(sent).isNotNull();
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

        @Test
        @DisplayName("should remove attribute")
        void shouldRemoveAttribute() {
            session.setAttribute("key", "value");
            session.removeAttribute("key");

            String value = session.getAttribute("key");
            assertThat(value).isNull();
        }

        @Test
        @DisplayName("should support different attribute types")
        void shouldSupportDifferentAttributeTypes() {
            session.setAttribute("string", "value");
            session.setAttribute("int", 42);
            session.setAttribute("object", new Object());

            assertThat((String) session.getAttribute("string")).isEqualTo("value");
            assertThat((Integer) session.getAttribute("int")).isEqualTo(42);
            assertThat((Object) session.getAttribute("object")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("should start with zero statistics")
        void shouldStartWithZeroStatistics() {
            assertThat(session.getSubmitSmReceived()).isZero();
            assertThat(session.getDeliverSmSent()).isZero();
            assertThat(session.getErrors()).isZero();
        }

        @Test
        @DisplayName("should increment submit_sm counter")
        void shouldIncrementSubmitSmCounter() {
            session.incrementSubmitSmReceived();
            session.incrementSubmitSmReceived();

            assertThat(session.getSubmitSmReceived()).isEqualTo(2);
        }

        @Test
        @DisplayName("should increment error counter")
        void shouldIncrementErrorCounter() {
            session.incrementErrors();

            assertThat(session.getErrors()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Channel Tests")
    class ChannelTests {

        @Test
        @DisplayName("should close channel")
        void shouldCloseChannel() {
            session.close();

            assertThat(channel.isOpen()).isFalse();
        }

        @Test
        @DisplayName("should return channel")
        void shouldReturnChannel() {
            assertThat(session.getChannel()).isEqualTo(channel);
        }
    }

    @Test
    @DisplayName("should get session id")
    void shouldGetSessionId() {
        session.setBound(SmppBindType.TRANSCEIVER, "TestSMSC", "pw", "type", (byte) 0x34);

        assertThat(session.getSessionId()).isNotNull();
        assertThat(session.getSystemId()).isEqualTo("TestSMSC");
        assertThat(session.getState()).isEqualTo(SessionState.BOUND_TRX);
    }
}
