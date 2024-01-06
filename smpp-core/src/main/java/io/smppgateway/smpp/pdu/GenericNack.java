package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Generic Negative Acknowledgement PDU.
 * <p>
 * Sent when a PDU cannot be decoded or is malformed. Uses the sequence
 * number from the problematic PDU if known, or 0 if not.
 *
 * @param sequenceNumber the sequence number from the problematic PDU
 * @param commandStatus  the error status indicating what went wrong
 */
public record GenericNack(
        int sequenceNumber,
        CommandStatus commandStatus
) implements Pdu {

    /**
     * Creates a GenericNack for an invalid command ID.
     */
    public static GenericNack invalidCommandId(int sequenceNumber) {
        return new GenericNack(sequenceNumber, CommandStatus.ESME_RINVCMDID);
    }

    /**
     * Creates a GenericNack for an invalid command length.
     */
    public static GenericNack invalidCommandLength(int sequenceNumber) {
        return new GenericNack(sequenceNumber, CommandStatus.ESME_RINVCMDLEN);
    }

    /**
     * Creates a GenericNack for an invalid message length.
     */
    public static GenericNack invalidMessageLength(int sequenceNumber) {
        return new GenericNack(sequenceNumber, CommandStatus.ESME_RINVMSGLEN);
    }

    /**
     * Creates a GenericNack for a system error.
     */
    public static GenericNack systemError(int sequenceNumber) {
        return new GenericNack(sequenceNumber, CommandStatus.ESME_RSYSERR);
    }

    @Override
    public CommandId commandId() {
        return CommandId.GENERIC_NACK;
    }

    @Override
    public List<Tlv> optionalParameters() {
        return List.of();
    }
}
