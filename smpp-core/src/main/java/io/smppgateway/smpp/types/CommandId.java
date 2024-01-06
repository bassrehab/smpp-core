package io.smppgateway.smpp.types;

/**
 * SMPP command identifiers as defined in SMPP 3.4 and 5.0 specifications.
 * Response command IDs have the high bit set (0x80000000).
 */
public enum CommandId {
    // Bind operations
    BIND_RECEIVER(0x00000001),
    BIND_RECEIVER_RESP(0x80000001),
    BIND_TRANSMITTER(0x00000002),
    BIND_TRANSMITTER_RESP(0x80000002),
    BIND_TRANSCEIVER(0x00000009),
    BIND_TRANSCEIVER_RESP(0x80000009),
    OUTBIND(0x0000000B),

    // Message operations
    SUBMIT_SM(0x00000004),
    SUBMIT_SM_RESP(0x80000004),
    SUBMIT_MULTI(0x00000021),
    SUBMIT_MULTI_RESP(0x80000021),
    DELIVER_SM(0x00000005),
    DELIVER_SM_RESP(0x80000005),
    DATA_SM(0x00000103),
    DATA_SM_RESP(0x80000103),

    // Query operations
    QUERY_SM(0x00000003),
    QUERY_SM_RESP(0x80000003),

    // Cancel and replace
    CANCEL_SM(0x00000008),
    CANCEL_SM_RESP(0x80000008),
    REPLACE_SM(0x00000007),
    REPLACE_SM_RESP(0x80000007),

    // Session management
    UNBIND(0x00000006),
    UNBIND_RESP(0x80000006),
    ENQUIRE_LINK(0x00000015),
    ENQUIRE_LINK_RESP(0x80000015),

    // Error
    GENERIC_NACK(0x80000000),

    // Alert
    ALERT_NOTIFICATION(0x00000102);

    private final int code;

    CommandId(int code) {
        this.code = code;
    }

    /**
     * Returns the integer command ID code.
     */
    public int code() {
        return code;
    }

    /**
     * Returns true if this is a response command ID (high bit set).
     */
    public boolean isResponse() {
        return (code & 0x80000000) != 0;
    }

    /**
     * Returns true if this is a request command ID.
     */
    public boolean isRequest() {
        return !isResponse() && this != GENERIC_NACK;
    }

    /**
     * Finds the CommandId for the given code.
     *
     * @param code the command ID code
     * @return the CommandId, or null if not found
     */
    public static CommandId fromCode(int code) {
        for (CommandId cmd : values()) {
            if (cmd.code == code) {
                return cmd;
            }
        }
        return null;
    }

    /**
     * Returns the response CommandId for this request CommandId.
     *
     * @return the response CommandId
     * @throws IllegalStateException if this is already a response
     */
    public CommandId toResponse() {
        if (isResponse()) {
            throw new IllegalStateException("Already a response: " + this);
        }
        return fromCode(code | 0x80000000);
    }
}
