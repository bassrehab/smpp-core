package io.smppgateway.smpp.types;

/**
 * ESM class field values for SMPP messages.
 * The ESM class byte contains messaging mode, message type, and GSM features.
 */
public record EsmClass(byte value) {

    // Messaging Mode (bits 1-0)
    public static final byte MODE_MASK = 0x03;
    public static final byte MODE_DEFAULT = 0x00;
    public static final byte MODE_DATAGRAM = 0x01;
    public static final byte MODE_FORWARD = 0x02;
    public static final byte MODE_STORE_AND_FORWARD = 0x03;

    // Message Type (bits 5-2)
    public static final byte TYPE_MASK = 0x3C;
    public static final byte TYPE_DEFAULT = 0x00;
    public static final byte TYPE_DELIVERY_RECEIPT = 0x04;
    public static final byte TYPE_DELIVERY_ACK = 0x08;
    public static final byte TYPE_USER_ACK = 0x10;
    public static final byte TYPE_CONVERSATION_ABORT = 0x18;
    public static final byte TYPE_INTERMEDIATE_NOTIFICATION = 0x20;

    // GSM Network Features (bits 7-6)
    public static final byte GSM_MASK = (byte) 0xC0;
    public static final byte GSM_NO_FEATURES = 0x00;
    public static final byte GSM_UDHI = 0x40;           // User Data Header Indicator
    public static final byte GSM_REPLY_PATH = (byte) 0x80;
    public static final byte GSM_UDHI_AND_REPLY = (byte) 0xC0;

    /**
     * Default ESM class (no special features).
     */
    public static final EsmClass DEFAULT = new EsmClass((byte) 0x00);

    /**
     * Delivery receipt ESM class.
     */
    public static final EsmClass DELIVERY_RECEIPT = new EsmClass(TYPE_DELIVERY_RECEIPT);

    /**
     * Returns the messaging mode.
     */
    public byte mode() {
        return (byte) (value & MODE_MASK);
    }

    /**
     * Returns the message type.
     */
    public byte messageType() {
        return (byte) (value & TYPE_MASK);
    }

    /**
     * Returns the GSM features.
     */
    public byte gsmFeatures() {
        return (byte) (value & GSM_MASK);
    }

    /**
     * Returns true if this is a delivery receipt.
     */
    public boolean isDeliveryReceipt() {
        return messageType() == TYPE_DELIVERY_RECEIPT;
    }

    /**
     * Returns true if User Data Header is present.
     */
    public boolean hasUdhi() {
        return (value & GSM_UDHI) != 0;
    }

    /**
     * Returns true if Reply Path is set.
     */
    public boolean hasReplyPath() {
        return (value & GSM_REPLY_PATH) != 0;
    }

    /**
     * Creates an ESM class with the given mode.
     */
    public EsmClass withMode(byte mode) {
        return new EsmClass((byte) ((value & ~MODE_MASK) | (mode & MODE_MASK)));
    }

    /**
     * Creates an ESM class with the given message type.
     */
    public EsmClass withMessageType(byte type) {
        return new EsmClass((byte) ((value & ~TYPE_MASK) | (type & TYPE_MASK)));
    }

    /**
     * Creates an ESM class with UDHI set.
     */
    public EsmClass withUdhi() {
        return new EsmClass((byte) (value | GSM_UDHI));
    }

    /**
     * Creates an ESM class with Reply Path set.
     */
    public EsmClass withReplyPath() {
        return new EsmClass((byte) (value | GSM_REPLY_PATH));
    }

    /**
     * Creates an ESM class from a byte value.
     */
    public static EsmClass of(byte value) {
        return new EsmClass(value);
    }

    /**
     * Creates an ESM class from a byte value.
     */
    public static EsmClass fromByte(byte value) {
        return new EsmClass(value);
    }

    /**
     * Returns the byte value.
     */
    public byte toByte() {
        return value;
    }
}
