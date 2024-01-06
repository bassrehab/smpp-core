package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Outbind PDU - used by SMSC to initiate a connection to an ESME.
 * This is a special PDU that doesn't have a direct response.
 */
public record Outbind(
        int sequenceNumber,
        String systemId,
        String password,
        List<Tlv> optionalParameters
) implements PduRequest<BindTransceiverResp> {

    public Outbind {
        systemId = systemId != null ? systemId : "";
        password = password != null ? password : "";
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.OUTBIND;
    }

    @Override
    public BindTransceiverResp createResponse() {
        return createResponse(CommandStatus.OK);
    }

    @Override
    public BindTransceiverResp createResponse(CommandStatus status) {
        return new BindTransceiverResp(sequenceNumber, status, systemId, List.of());
    }
}
