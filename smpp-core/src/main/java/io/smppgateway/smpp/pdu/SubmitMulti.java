package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.*;

import java.util.List;

/**
 * Submit Multi PDU - submits a message to multiple destinations.
 */
public record SubmitMulti(
        int sequenceNumber,
        String serviceType,
        Address sourceAddress,
        List<Address> destAddresses,
        EsmClass esmClass,
        byte protocolId,
        byte priorityFlag,
        String scheduleDeliveryTime,
        String validityPeriod,
        RegisteredDelivery registeredDelivery,
        byte replaceIfPresent,
        DataCoding dataCoding,
        byte smDefaultMsgId,
        byte[] shortMessage,
        List<Tlv> optionalParameters
) implements PduRequest<SubmitMultiResp> {

    public SubmitMulti {
        serviceType = serviceType != null ? serviceType : "";
        destAddresses = destAddresses != null ? List.copyOf(destAddresses) : List.of();
        esmClass = esmClass != null ? esmClass : EsmClass.DEFAULT;
        scheduleDeliveryTime = scheduleDeliveryTime != null ? scheduleDeliveryTime : "";
        validityPeriod = validityPeriod != null ? validityPeriod : "";
        registeredDelivery = registeredDelivery != null ? registeredDelivery : RegisteredDelivery.NONE;
        dataCoding = dataCoding != null ? dataCoding : DataCoding.DEFAULT;
        shortMessage = shortMessage != null ? shortMessage.clone() : new byte[0];
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.SUBMIT_MULTI;
    }

    @Override
    public SubmitMultiResp createResponse() {
        return createResponse(CommandStatus.OK);
    }

    @Override
    public SubmitMultiResp createResponse(CommandStatus status) {
        return new SubmitMultiResp(sequenceNumber, status, null, List.of(), List.of());
    }
}
