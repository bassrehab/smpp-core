package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Replace Short Message Response PDU.
 */
public record ReplaceSmResp(
        int sequenceNumber,
        CommandStatus commandStatus
) implements PduResponse {

    @Override
    public CommandId commandId() {
        return CommandId.REPLACE_SM_RESP;
    }

    @Override
    public List<Tlv> optionalParameters() {
        return List.of();
    }
}
