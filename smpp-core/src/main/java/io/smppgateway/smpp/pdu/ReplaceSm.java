package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.*;

import java.util.List;

/**
 * Replace Short Message PDU - replaces a previously submitted message.
 */
public record ReplaceSm(
        int sequenceNumber,
        String messageId,
        Address sourceAddress,
        String scheduleDeliveryTime,
        String validityPeriod,
        RegisteredDelivery registeredDelivery,
        byte smDefaultMsgId,
        byte[] shortMessage,
        List<Tlv> optionalParameters
) implements PduRequest<ReplaceSmResp> {

    public ReplaceSm {
        scheduleDeliveryTime = scheduleDeliveryTime != null ? scheduleDeliveryTime : "";
        validityPeriod = validityPeriod != null ? validityPeriod : "";
        registeredDelivery = registeredDelivery != null ? registeredDelivery : RegisteredDelivery.NONE;
        shortMessage = shortMessage != null ? shortMessage.clone() : new byte[0];
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.REPLACE_SM;
    }

    @Override
    public ReplaceSmResp createResponse() {
        return createResponse(CommandStatus.OK);
    }

    @Override
    public ReplaceSmResp createResponse(CommandStatus status) {
        return new ReplaceSmResp(sequenceNumber, status);
    }
}
