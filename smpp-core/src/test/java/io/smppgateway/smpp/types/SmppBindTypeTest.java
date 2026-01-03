package io.smppgateway.smpp.types;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppBindType Tests")
class SmppBindTypeTest {

    @Test
    @DisplayName("TRANSMITTER should have correct bind command ID")
    void transmitterShouldHaveCorrectBindCommandId() {
        assertThat(SmppBindType.TRANSMITTER.bindCommandId()).isEqualTo(CommandId.BIND_TRANSMITTER);
    }

    @Test
    @DisplayName("RECEIVER should have correct bind command ID")
    void receiverShouldHaveCorrectBindCommandId() {
        assertThat(SmppBindType.RECEIVER.bindCommandId()).isEqualTo(CommandId.BIND_RECEIVER);
    }

    @Test
    @DisplayName("TRANSCEIVER should have correct bind command ID")
    void transceiverShouldHaveCorrectBindCommandId() {
        assertThat(SmppBindType.TRANSCEIVER.bindCommandId()).isEqualTo(CommandId.BIND_TRANSCEIVER);
    }

    @Test
    @DisplayName("TRANSMITTER can transmit but cannot receive")
    void transmitterCanTransmitButCannotReceive() {
        assertThat(SmppBindType.TRANSMITTER.canTransmit()).isTrue();
        assertThat(SmppBindType.TRANSMITTER.canReceive()).isFalse();
    }

    @Test
    @DisplayName("RECEIVER can receive but cannot transmit")
    void receiverCanReceiveButCannotTransmit() {
        assertThat(SmppBindType.RECEIVER.canReceive()).isTrue();
        assertThat(SmppBindType.RECEIVER.canTransmit()).isFalse();
    }

    @Test
    @DisplayName("TRANSCEIVER can both transmit and receive")
    void transceiverCanBothTransmitAndReceive() {
        assertThat(SmppBindType.TRANSCEIVER.canTransmit()).isTrue();
        assertThat(SmppBindType.TRANSCEIVER.canReceive()).isTrue();
    }

    @Test
    @DisplayName("should resolve TRANSMITTER from bind command IDs")
    void shouldResolveTransmitterFromBindCommandIds() {
        assertThat(SmppBindType.fromCommandId(CommandId.BIND_TRANSMITTER)).isEqualTo(SmppBindType.TRANSMITTER);
        assertThat(SmppBindType.fromCommandId(CommandId.BIND_TRANSMITTER_RESP)).isEqualTo(SmppBindType.TRANSMITTER);
    }

    @Test
    @DisplayName("should resolve RECEIVER from bind command IDs")
    void shouldResolveReceiverFromBindCommandIds() {
        assertThat(SmppBindType.fromCommandId(CommandId.BIND_RECEIVER)).isEqualTo(SmppBindType.RECEIVER);
        assertThat(SmppBindType.fromCommandId(CommandId.BIND_RECEIVER_RESP)).isEqualTo(SmppBindType.RECEIVER);
    }

    @Test
    @DisplayName("should resolve TRANSCEIVER from bind command IDs")
    void shouldResolveTransceiverFromBindCommandIds() {
        assertThat(SmppBindType.fromCommandId(CommandId.BIND_TRANSCEIVER)).isEqualTo(SmppBindType.TRANSCEIVER);
        assertThat(SmppBindType.fromCommandId(CommandId.BIND_TRANSCEIVER_RESP)).isEqualTo(SmppBindType.TRANSCEIVER);
    }

    @Test
    @DisplayName("should return null for non-bind command IDs")
    void shouldReturnNullForNonBindCommandIds() {
        assertThat(SmppBindType.fromCommandId(CommandId.SUBMIT_SM)).isNull();
        assertThat(SmppBindType.fromCommandId(CommandId.DELIVER_SM)).isNull();
        assertThat(SmppBindType.fromCommandId(CommandId.ENQUIRE_LINK)).isNull();
        assertThat(SmppBindType.fromCommandId(CommandId.UNBIND)).isNull();
        assertThat(SmppBindType.fromCommandId(CommandId.GENERIC_NACK)).isNull();
    }

    @Test
    @DisplayName("should have three bind types")
    void shouldHaveThreeBindTypes() {
        assertThat(SmppBindType.values()).hasSize(3);
    }
}
