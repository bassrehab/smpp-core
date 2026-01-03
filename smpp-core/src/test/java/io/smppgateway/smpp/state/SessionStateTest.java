package io.smppgateway.smpp.state;

import io.smppgateway.smpp.types.SmppBindType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SessionState Tests")
class SessionStateTest {

    @Test
    @DisplayName("should have five session states")
    void shouldHaveFiveSessionStates() {
        assertThat(SessionState.values()).hasSize(5);
    }

    @Test
    @DisplayName("should correctly identify bound states")
    void shouldIdentifyBoundStates() {
        assertThat(SessionState.BOUND_TX.isBound()).isTrue();
        assertThat(SessionState.BOUND_RX.isBound()).isTrue();
        assertThat(SessionState.BOUND_TRX.isBound()).isTrue();

        assertThat(SessionState.CLOSED.isBound()).isFalse();
        assertThat(SessionState.OPEN.isBound()).isFalse();
    }

    @Test
    @DisplayName("should correctly identify transmit capability")
    void shouldIdentifyTransmitCapability() {
        assertThat(SessionState.BOUND_TX.canTransmit()).isTrue();
        assertThat(SessionState.BOUND_TRX.canTransmit()).isTrue();

        assertThat(SessionState.BOUND_RX.canTransmit()).isFalse();
        assertThat(SessionState.CLOSED.canTransmit()).isFalse();
        assertThat(SessionState.OPEN.canTransmit()).isFalse();
    }

    @Test
    @DisplayName("should correctly identify receive capability")
    void shouldIdentifyReceiveCapability() {
        assertThat(SessionState.BOUND_RX.canReceive()).isTrue();
        assertThat(SessionState.BOUND_TRX.canReceive()).isTrue();

        assertThat(SessionState.BOUND_TX.canReceive()).isFalse();
        assertThat(SessionState.CLOSED.canReceive()).isFalse();
        assertThat(SessionState.OPEN.canReceive()).isFalse();
    }

    @Test
    @DisplayName("BOUND_TRX should have both transmit and receive capability")
    void boundTrxShouldHaveBothCapabilities() {
        assertThat(SessionState.BOUND_TRX.canTransmit()).isTrue();
        assertThat(SessionState.BOUND_TRX.canReceive()).isTrue();
    }

    @Test
    @DisplayName("should convert from TRANSMITTER bind type")
    void shouldConvertFromTransmitterBindType() {
        assertThat(SessionState.fromBindType(SmppBindType.TRANSMITTER))
                .isEqualTo(SessionState.BOUND_TX);
    }

    @Test
    @DisplayName("should convert from RECEIVER bind type")
    void shouldConvertFromReceiverBindType() {
        assertThat(SessionState.fromBindType(SmppBindType.RECEIVER))
                .isEqualTo(SessionState.BOUND_RX);
    }

    @Test
    @DisplayName("should convert from TRANSCEIVER bind type")
    void shouldConvertFromTransceiverBindType() {
        assertThat(SessionState.fromBindType(SmppBindType.TRANSCEIVER))
                .isEqualTo(SessionState.BOUND_TRX);
    }

    @ParameterizedTest
    @EnumSource(SmppBindType.class)
    @DisplayName("all bind types should map to a bound state")
    void allBindTypesShouldMapToBoundState(SmppBindType bindType) {
        SessionState state = SessionState.fromBindType(bindType);
        assertThat(state.isBound()).isTrue();
    }
}
