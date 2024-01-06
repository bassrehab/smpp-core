package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;
import java.util.Optional;

/**
 * Base sealed interface for all SMPP Protocol Data Units (PDUs).
 * <p>
 * Every PDU has a 16-byte header containing:
 * <ul>
 *   <li>command_length (4 bytes) - total PDU size including header</li>
 *   <li>command_id (4 bytes) - identifies the PDU type</li>
 *   <li>command_status (4 bytes) - success/error code (0 for requests)</li>
 *   <li>sequence_number (4 bytes) - correlates requests with responses</li>
 * </ul>
 * <p>
 * PDUs may also contain optional TLV (Tag-Length-Value) parameters at the end.
 */
public sealed interface Pdu permits PduRequest, PduResponse, GenericNack {

    /**
     * SMPP PDU header size in bytes.
     */
    int HEADER_SIZE = 16;

    /**
     * Returns the command ID identifying this PDU type.
     */
    CommandId commandId();

    /**
     * Returns the command status. Always OK for request PDUs.
     */
    CommandStatus commandStatus();

    /**
     * Returns the sequence number used to correlate requests and responses.
     */
    int sequenceNumber();

    /**
     * Returns the list of optional TLV parameters.
     */
    List<Tlv> optionalParameters();

    /**
     * Finds a TLV by its tag code.
     *
     * @param tag the TLV tag code
     * @return the TLV if found
     */
    default Optional<Tlv> findTlv(short tag) {
        return optionalParameters().stream()
                .filter(tlv -> tlv.tag() == tag)
                .findFirst();
    }

    /**
     * Finds a TLV by its standard tag.
     *
     * @param tag the standard TLV tag
     * @return the TLV if found
     */
    default Optional<Tlv> findTlv(io.smppgateway.smpp.pdu.tlv.TlvTag tag) {
        return findTlv(tag.code());
    }

    /**
     * Returns true if this is a request PDU.
     */
    default boolean isRequest() {
        return this instanceof PduRequest<?>;
    }

    /**
     * Returns true if this is a response PDU.
     */
    default boolean isResponse() {
        return this instanceof PduResponse;
    }

    /**
     * Returns the name of this PDU type (e.g., "submit_sm", "deliver_sm_resp").
     */
    default String name() {
        return commandId().name().toLowerCase();
    }
}
