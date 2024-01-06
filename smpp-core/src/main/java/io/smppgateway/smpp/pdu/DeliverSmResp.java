package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Deliver Short Message Response PDU.
 */
public record DeliverSmResp(
        int sequenceNumber,
        CommandStatus commandStatus,
        String messageId,
        List<Tlv> optionalParameters
) implements PduResponse {

    public DeliverSmResp {
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.DELIVER_SM_RESP;
    }
}
