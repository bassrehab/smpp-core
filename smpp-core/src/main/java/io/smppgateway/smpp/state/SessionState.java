package io.smppgateway.smpp.state;

import io.smppgateway.smpp.types.SmppBindType;

/**
 * SMPP session states as defined in the SMPP specification.
 */
public enum SessionState {
    /**
     * Initial state before connection or after unbind/error.
     * No SMPP operations are permitted.
     */
    CLOSED,

    /**
     * TCP connection established but not yet bound.
     * Only bind operations are permitted.
     */
    OPEN,

    /**
     * Session bound in transmitter mode.
     * Can submit messages but cannot receive.
     */
    BOUND_TX,

    /**
     * Session bound in receiver mode.
     * Can receive messages but cannot submit.
     */
    BOUND_RX,

    /**
     * Session bound in transceiver mode.
     * Can both submit and receive messages.
     */
    BOUND_TRX;

    /**
     * Returns true if the session is bound (TX, RX, or TRX).
     */
    public boolean isBound() {
        return this == BOUND_TX || this == BOUND_RX || this == BOUND_TRX;
    }

    /**
     * Returns true if the session can transmit messages.
     */
    public boolean canTransmit() {
        return this == BOUND_TX || this == BOUND_TRX;
    }

    /**
     * Returns true if the session can receive messages.
     */
    public boolean canReceive() {
        return this == BOUND_RX || this == BOUND_TRX;
    }

    /**
     * Returns the bound state for the given bind type.
     *
     * @param bindType the bind type
     * @return the corresponding bound state
     */
    public static SessionState fromBindType(SmppBindType bindType) {
        return switch (bindType) {
            case TRANSMITTER -> BOUND_TX;
            case RECEIVER -> BOUND_RX;
            case TRANSCEIVER -> BOUND_TRX;
        };
    }
}
