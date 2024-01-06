package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Submit Multi Response PDU.
 */
public record SubmitMultiResp(
        int sequenceNumber,
        CommandStatus commandStatus,
        String messageId,
        List<UnsuccessfulDelivery> unsuccessfulDeliveries,
        List<Tlv> optionalParameters
) implements PduResponse {

    /**
     * Represents a failed delivery to a specific address.
     */
    public record UnsuccessfulDelivery(Address destAddress, CommandStatus errorStatus) {}

    public SubmitMultiResp {
        unsuccessfulDeliveries = unsuccessfulDeliveries != null ? List.copyOf(unsuccessfulDeliveries) : List.of();
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.SUBMIT_MULTI_RESP;
    }
}
