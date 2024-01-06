package io.smppgateway.smpp.pdu.tlv;

/**
 * Standard SMPP TLV (Tag-Length-Value) tags as defined in SMPP 3.4 and 5.0.
 */
public enum TlvTag {
    // Destination address tags
    DEST_ADDR_SUBUNIT(0x0005),
    DEST_NETWORK_TYPE(0x0006),
    DEST_BEARER_TYPE(0x0007),
    DEST_TELEMATICS_ID(0x0008),

    // Source address tags
    SOURCE_ADDR_SUBUNIT(0x000D),
    SOURCE_NETWORK_TYPE(0x000E),
    SOURCE_BEARER_TYPE(0x000F),
    SOURCE_TELEMATICS_ID(0x0010),

    // QoS and timing
    QOS_TIME_TO_LIVE(0x0017),
    PAYLOAD_TYPE(0x0019),

    // Message handling
    ADDITIONAL_STATUS_INFO_TEXT(0x001D),
    RECEIPTED_MESSAGE_ID(0x001E),

    // Message segmentation
    MS_MSG_WAIT_FACILITIES(0x0030),

    // Privacy and priority
    PRIVACY_INDICATOR(0x0201),
    SOURCE_SUBADDRESS(0x0202),
    DEST_SUBADDRESS(0x0203),

    // User message reference
    USER_MESSAGE_REFERENCE(0x0204),
    USER_RESPONSE_CODE(0x0205),

    // Port numbers
    SOURCE_PORT(0x020A),
    DESTINATION_PORT(0x020B),

    // SAR (Segmentation and Reassembly)
    SAR_MSG_REF_NUM(0x020C),
    LANGUAGE_INDICATOR(0x020D),
    SAR_TOTAL_SEGMENTS(0x020E),
    SAR_SEGMENT_SEQNUM(0x020F),

    // Service class
    SC_INTERFACE_VERSION(0x0210),

    // Callback numbers
    CALLBACK_NUM_PRES_IND(0x0302),
    CALLBACK_NUM_ATAG(0x0303),
    NUMBER_OF_MESSAGES(0x0304),
    CALLBACK_NUM(0x0381),

    // DPF result
    DPF_RESULT(0x0420),

    // Set DPF
    SET_DPF(0x0421),

    // MS availability status
    MS_AVAILABILITY_STATUS(0x0422),

    // Network error code
    NETWORK_ERROR_CODE(0x0423),

    // Message payload (for messages > 255 bytes)
    MESSAGE_PAYLOAD(0x0424),

    // Delivery failure reason
    DELIVERY_FAILURE_REASON(0x0425),

    // More messages to send
    MORE_MESSAGES_TO_SEND(0x0426),

    // Message state
    MESSAGE_STATE(0x0427),

    // Congestion state (SMPP 5.0)
    CONGESTION_STATE(0x0428),

    // USSD service operation
    USSD_SERVICE_OP(0x0501),

    // Broadcast tags (SMPP 5.0)
    BROADCAST_CHANNEL_INDICATOR(0x0600),
    BROADCAST_CONTENT_TYPE(0x0601),
    BROADCAST_CONTENT_TYPE_INFO(0x0602),
    BROADCAST_MESSAGE_CLASS(0x0603),
    BROADCAST_REP_NUM(0x0604),
    BROADCAST_FREQUENCY_INTERVAL(0x0605),
    BROADCAST_AREA_IDENTIFIER(0x0606),
    BROADCAST_ERROR_STATUS(0x0607),
    BROADCAST_AREA_SUCCESS(0x0608),
    BROADCAST_END_TIME(0x0609),
    BROADCAST_SERVICE_GROUP(0x060A),
    BILLING_IDENTIFICATION(0x060B),

    // Source network
    SOURCE_NETWORK_ID(0x060D),
    DEST_NETWORK_ID(0x060E),
    SOURCE_NODE_ID(0x060F),
    DEST_NODE_ID(0x0610),

    // Number portability
    DEST_ADDR_NP_RESOLUTION(0x0611),
    DEST_ADDR_NP_INFORMATION(0x0612),
    DEST_ADDR_NP_COUNTRY(0x0613),

    // Display time
    DISPLAY_TIME(0x1201),

    // SMS signal
    SMS_SIGNAL(0x1203),

    // MS validity
    MS_VALIDITY(0x1204),

    // Alert on message delivery
    ALERT_ON_MESSAGE_DELIVERY(0x130C),

    // ITS related
    ITS_REPLY_TYPE(0x1380),
    ITS_SESSION_INFO(0x1383);

    private final short code;

    TlvTag(int code) {
        this.code = (short) code;
    }

    /**
     * Returns the TLV tag code.
     */
    public short code() {
        return code;
    }

    /**
     * Finds the TlvTag for the given code.
     *
     * @param code the TLV tag code
     * @return the TlvTag, or null if not a standard tag
     */
    public static TlvTag fromCode(short code) {
        for (TlvTag tag : values()) {
            if (tag.code == code) {
                return tag;
            }
        }
        return null;
    }
}
