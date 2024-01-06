package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Enquire Link PDU - used for keep-alive/heartbeat.
 */
public record EnquireLink(
        int sequenceNumber
) implements PduRequest<EnquireLinkResp> {

    @Override
    public CommandId commandId() {
        return CommandId.ENQUIRE_LINK;
    }

    @Override
    public List<Tlv> optionalParameters() {
        return List.of();
    }

    @Override
    public EnquireLinkResp createResponse() {
        return createResponse(CommandStatus.OK);
    }

    @Override
    public EnquireLinkResp createResponse(CommandStatus status) {
        return new EnquireLinkResp(sequenceNumber, status);
    }
}
