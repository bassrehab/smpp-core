package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Bind Transceiver Response PDU.
 */
public record BindTransceiverResp(
        int sequenceNumber,
        CommandStatus commandStatus,
        String systemId,
        List<Tlv> optionalParameters
) implements PduResponse {

    public BindTransceiverResp {
        systemId = systemId != null ? systemId : "";
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.BIND_TRANSCEIVER_RESP;
    }
}
