package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Unbind PDU - gracefully terminates a session.
 */
public record Unbind(
        int sequenceNumber
) implements PduRequest<UnbindResp> {

    @Override
    public CommandId commandId() {
        return CommandId.UNBIND;
    }

    @Override
    public List<Tlv> optionalParameters() {
        return List.of();
    }

    @Override
    public UnbindResp createResponse() {
        return createResponse(CommandStatus.OK);
    }

    @Override
    public UnbindResp createResponse(CommandStatus status) {
        return new UnbindResp(sequenceNumber, status);
    }
}
