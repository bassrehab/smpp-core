package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Bind Receiver Response PDU.
 */
public record BindReceiverResp(
        int sequenceNumber,
        CommandStatus commandStatus,
        String systemId,
        List<Tlv> optionalParameters
) implements PduResponse {

    public BindReceiverResp {
        systemId = systemId != null ? systemId : "";
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.BIND_RECEIVER_RESP;
    }
}
