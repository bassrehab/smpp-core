package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Submit Short Message Response PDU.
 */
public record SubmitSmResp(
        int sequenceNumber,
        CommandStatus commandStatus,
        String messageId,
        List<Tlv> optionalParameters
) implements PduResponse {

    public SubmitSmResp {
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.SUBMIT_SM_RESP;
    }

    /**
     * Returns true if this response contains a message ID.
     */
    public boolean hasMessageId() {
        return messageId != null && !messageId.isEmpty();
    }
}
