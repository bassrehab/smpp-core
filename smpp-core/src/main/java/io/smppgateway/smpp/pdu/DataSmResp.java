package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Data Short Message Response PDU.
 */
public record DataSmResp(
        int sequenceNumber,
        CommandStatus commandStatus,
        String messageId,
        List<Tlv> optionalParameters
) implements PduResponse {

    public DataSmResp {
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.DATA_SM_RESP;
    }
}
