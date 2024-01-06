package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Enquire Link Response PDU.
 */
public record EnquireLinkResp(
        int sequenceNumber,
        CommandStatus commandStatus
) implements PduResponse {

    @Override
    public CommandId commandId() {
        return CommandId.ENQUIRE_LINK_RESP;
    }

    @Override
    public List<Tlv> optionalParameters() {
        return List.of();
    }
}
