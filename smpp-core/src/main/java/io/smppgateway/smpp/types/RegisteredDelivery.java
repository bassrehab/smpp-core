package io.smppgateway.smpp.types;

/**
 * Registered delivery field values for SMPP messages.
 * Controls delivery receipt and acknowledgement requests.
 */
public record RegisteredDelivery(byte value) {

    // SMSC Delivery Receipt (bits 1-0)
    public static final byte RECEIPT_MASK = 0x03;
    public static final byte RECEIPT_NONE = 0x00;
    public static final byte RECEIPT_REQUESTED = 0x01;
    public static final byte RECEIPT_ON_FAILURE = 0x02;
    public static final byte RECEIPT_ON_SUCCESS = 0x03;

    // SME Originated Acknowledgement (bits 3-2)
    public static final byte ACK_MASK = 0x0C;
    public static final byte ACK_NONE = 0x00;
    public static final byte ACK_DELIVERY = 0x04;
    public static final byte ACK_USER = 0x08;
    public static final byte ACK_BOTH = 0x0C;

    // Intermediate Notification (bit 4)
    public static final byte INTERMEDIATE_MASK = 0x10;
    public static final byte INTERMEDIATE_NONE = 0x00;
    public static final byte INTERMEDIATE_REQUESTED = 0x10;

    /**
     * No delivery receipt requested.
     */
    public static final RegisteredDelivery NONE = new RegisteredDelivery((byte) 0x00);

    /**
     * Delivery receipt requested for all outcomes.
     */
    public static final RegisteredDelivery SMSC_DELIVERY_RECEIPT_REQUESTED = new RegisteredDelivery(RECEIPT_REQUESTED);

    /**
     * Delivery receipt only on failure.
     */
    public static final RegisteredDelivery SMSC_DELIVERY_RECEIPT_ON_FAILURE = new RegisteredDelivery(RECEIPT_ON_FAILURE);

    /**
     * Delivery receipt only on success.
     */
    public static final RegisteredDelivery SMSC_DELIVERY_RECEIPT_ON_SUCCESS = new RegisteredDelivery(RECEIPT_ON_SUCCESS);

    /**
     * Returns true if a delivery receipt is requested.
     */
    public boolean isDeliveryReceiptRequested() {
        return (value & RECEIPT_MASK) != RECEIPT_NONE;
    }

    /**
     * Returns the delivery receipt type.
     */
    public byte receiptType() {
        return (byte) (value & RECEIPT_MASK);
    }

    /**
     * Returns true if acknowledgement is requested.
     */
    public boolean isAcknowledgementRequested() {
        return (value & ACK_MASK) != ACK_NONE;
    }

    /**
     * Returns true if intermediate notification is requested.
     */
    public boolean isIntermediateNotificationRequested() {
        return (value & INTERMEDIATE_MASK) != INTERMEDIATE_NONE;
    }

    /**
     * Creates a RegisteredDelivery with delivery receipt requested.
     */
    public RegisteredDelivery withDeliveryReceipt() {
        return new RegisteredDelivery((byte) ((value & ~RECEIPT_MASK) | RECEIPT_REQUESTED));
    }

    /**
     * Creates a RegisteredDelivery with delivery acknowledgement requested.
     */
    public RegisteredDelivery withDeliveryAck() {
        return new RegisteredDelivery((byte) (value | ACK_DELIVERY));
    }

    /**
     * Creates a RegisteredDelivery with user acknowledgement requested.
     */
    public RegisteredDelivery withUserAck() {
        return new RegisteredDelivery((byte) (value | ACK_USER));
    }

    /**
     * Creates a RegisteredDelivery with intermediate notification requested.
     */
    public RegisteredDelivery withIntermediateNotification() {
        return new RegisteredDelivery((byte) (value | INTERMEDIATE_REQUESTED));
    }

    /**
     * Creates a RegisteredDelivery from a byte value.
     */
    public static RegisteredDelivery of(byte value) {
        return new RegisteredDelivery(value);
    }
}
