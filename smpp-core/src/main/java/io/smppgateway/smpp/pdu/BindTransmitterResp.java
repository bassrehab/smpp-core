package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Bind Transmitter Response PDU.
 */
public record BindTransmitterResp(
        int sequenceNumber,
        CommandStatus commandStatus,
        String systemId,
        List<Tlv> optionalParameters
) implements PduResponse {

    public BindTransmitterResp {
        systemId = systemId != null ? systemId : "";
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.BIND_TRANSMITTER_RESP;
    }
}
