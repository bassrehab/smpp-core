package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Submit Short Message PDU - used to send an SMS message.
 */
public record SubmitSm(
        int sequenceNumber,
        String serviceType,
        Address sourceAddress,
        Address destAddress,
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
) implements PduRequest<SubmitSmResp> {

    public static final int MAX_SHORT_MESSAGE_LENGTH = 254;

    public SubmitSm {
        Objects.requireNonNull(sourceAddress, "sourceAddress required");
        Objects.requireNonNull(destAddress, "destAddress required");
        serviceType = serviceType != null ? serviceType : "";
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
        return CommandId.SUBMIT_SM;
    }

    @Override
    public SubmitSmResp createResponse() {
        return createResponse(CommandStatus.OK);
    }

    @Override
    public SubmitSmResp createResponse(CommandStatus status) {
        return new SubmitSmResp(sequenceNumber, status, null, List.of());
    }

    @Override
    public SubmitSmResp createResponse(CommandStatus status, String messageId) {
        return new SubmitSmResp(sequenceNumber, status, messageId, List.of());
    }

    /**
     * Returns a copy of the short message bytes.
     */
    @Override
    public byte[] shortMessage() {
        return shortMessage.clone();
    }

    /**
     * Returns the short message length.
     */
    public int shortMessageLength() {
        return shortMessage.length;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int sequenceNumber;
        private String serviceType = "";
        private Address sourceAddress;
        private Address destAddress;
        private EsmClass esmClass = EsmClass.DEFAULT;
        private byte protocolId;
        private byte priorityFlag;
        private String scheduleDeliveryTime = "";
        private String validityPeriod = "";
        private RegisteredDelivery registeredDelivery = RegisteredDelivery.NONE;
        private byte replaceIfPresent;
        private DataCoding dataCoding = DataCoding.DEFAULT;
        private byte smDefaultMsgId;
        private byte[] shortMessage = new byte[0];
        private final List<Tlv> tlvs = new ArrayList<>();

        public Builder sequenceNumber(int seq) { this.sequenceNumber = seq; return this; }
        public Builder serviceType(String type) { this.serviceType = type; return this; }
        public Builder sourceAddress(Address addr) { this.sourceAddress = addr; return this; }
        public Builder sourceAddress(byte ton, byte npi, String addr) {
            this.sourceAddress = new Address(ton, npi, addr); return this;
        }
        public Builder destAddress(Address addr) { this.destAddress = addr; return this; }
        public Builder destAddress(byte ton, byte npi, String addr) {
            this.destAddress = new Address(ton, npi, addr); return this;
        }
        public Builder esmClass(EsmClass esm) { this.esmClass = esm; return this; }
        public Builder protocolId(byte id) { this.protocolId = id; return this; }
        public Builder priorityFlag(byte flag) { this.priorityFlag = flag; return this; }
        public Builder scheduleDeliveryTime(String time) { this.scheduleDeliveryTime = time; return this; }
        public Builder validityPeriod(String period) { this.validityPeriod = period; return this; }
        public Builder registeredDelivery(RegisteredDelivery rd) { this.registeredDelivery = rd; return this; }
        public Builder requestDeliveryReceipt() {
            this.registeredDelivery = RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED;
            return this;
        }
        public Builder replaceIfPresent(byte flag) { this.replaceIfPresent = flag; return this; }
        public Builder dataCoding(DataCoding dc) { this.dataCoding = dc; return this; }
        public Builder smDefaultMsgId(byte id) { this.smDefaultMsgId = id; return this; }
        public Builder shortMessage(byte[] msg) { this.shortMessage = msg; return this; }
        public Builder addTlv(Tlv tlv) { this.tlvs.add(tlv); return this; }

        public SubmitSm build() {
            return new SubmitSm(sequenceNumber, serviceType, sourceAddress, destAddress,
                    esmClass, protocolId, priorityFlag, scheduleDeliveryTime, validityPeriod,
                    registeredDelivery, replaceIfPresent, dataCoding, smDefaultMsgId,
                    shortMessage, tlvs);
        }
    }
}
