package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.*;

import java.util.List;
import java.util.Objects;

/**
 * Data Short Message PDU - alternative to submit_sm for data messaging.
 */
public record DataSm(
        int sequenceNumber,
        String serviceType,
        Address sourceAddress,
        Address destAddress,
        EsmClass esmClass,
        RegisteredDelivery registeredDelivery,
        DataCoding dataCoding,
        List<Tlv> optionalParameters
) implements PduRequest<DataSmResp> {

    public DataSm {
        Objects.requireNonNull(sourceAddress, "sourceAddress required");
        Objects.requireNonNull(destAddress, "destAddress required");
        serviceType = serviceType != null ? serviceType : "";
        esmClass = esmClass != null ? esmClass : EsmClass.DEFAULT;
        registeredDelivery = registeredDelivery != null ? registeredDelivery : RegisteredDelivery.NONE;
        dataCoding = dataCoding != null ? dataCoding : DataCoding.DEFAULT;
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.DATA_SM;
    }

    @Override
    public DataSmResp createResponse() {
        return createResponse(CommandStatus.OK);
    }

    @Override
    public DataSmResp createResponse(CommandStatus status) {
        return new DataSmResp(sequenceNumber, status, null, List.of());
    }
}
