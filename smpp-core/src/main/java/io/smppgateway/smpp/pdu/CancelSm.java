package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Cancel Short Message PDU - cancels a previously submitted message.
 */
public record CancelSm(
        int sequenceNumber,
        String serviceType,
        String messageId,
        Address sourceAddress,
        Address destAddress,
        List<Tlv> optionalParameters
) implements PduRequest<CancelSmResp> {

    public CancelSm {
        serviceType = serviceType != null ? serviceType : "";
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.CANCEL_SM;
    }

    @Override
    public CancelSmResp createResponse() {
        return createResponse(CommandStatus.OK);
    }

    @Override
    public CancelSmResp createResponse(CommandStatus status) {
        return new CancelSmResp(sequenceNumber, status);
    }
}
