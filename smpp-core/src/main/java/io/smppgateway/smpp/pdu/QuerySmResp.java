package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Query Short Message Response PDU.
 */
public record QuerySmResp(
        int sequenceNumber,
        CommandStatus commandStatus,
        String messageId,
        String finalDate,
        byte messageState,
        byte errorCode,
        List<Tlv> optionalParameters
) implements PduResponse {

    public QuerySmResp {
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.QUERY_SM_RESP;
    }
}
