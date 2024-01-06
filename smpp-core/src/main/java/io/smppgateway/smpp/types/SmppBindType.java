package io.smppgateway.smpp.types;

/**
 * SMPP bind types defining the direction of communication.
 */
public enum SmppBindType {
    /**
     * Transmitter mode - can only send messages (MT).
     * Uses bind_transmitter command.
     */
    TRANSMITTER(CommandId.BIND_TRANSMITTER),

    /**
     * Receiver mode - can only receive messages (MO).
     * Uses bind_receiver command.
     */
    RECEIVER(CommandId.BIND_RECEIVER),

    /**
     * Transceiver mode - can send and receive messages.
     * Uses bind_transceiver command. This is the most common mode.
     */
    TRANSCEIVER(CommandId.BIND_TRANSCEIVER);

    private final CommandId bindCommandId;

    SmppBindType(CommandId bindCommandId) {
        this.bindCommandId = bindCommandId;
    }

    /**
     * Returns the command ID used for binding with this type.
     */
    public CommandId bindCommandId() {
        return bindCommandId;
    }

    /**
     * Returns true if this bind type can send messages.
     */
    public boolean canTransmit() {
        return this == TRANSMITTER || this == TRANSCEIVER;
    }

    /**
     * Returns true if this bind type can receive messages.
     */
    public boolean canReceive() {
        return this == RECEIVER || this == TRANSCEIVER;
    }

    /**
     * Determines the bind type from a bind command ID.
     *
     * @param commandId the bind command ID
     * @return the bind type, or null if not a bind command
     */
    public static SmppBindType fromCommandId(CommandId commandId) {
        return switch (commandId) {
            case BIND_TRANSMITTER, BIND_TRANSMITTER_RESP -> TRANSMITTER;
            case BIND_RECEIVER, BIND_RECEIVER_RESP -> RECEIVER;
            case BIND_TRANSCEIVER, BIND_TRANSCEIVER_RESP -> TRANSCEIVER;
            default -> null;
        };
    }
}
