package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Unbind Response PDU.
 */
public record UnbindResp(
        int sequenceNumber,
        CommandStatus commandStatus
) implements PduResponse {

    @Override
    public CommandId commandId() {
        return CommandId.UNBIND_RESP;
    }

    @Override
    public List<Tlv> optionalParameters() {
        return List.of();
    }
}
