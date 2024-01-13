package io.smppgateway.smpp.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Encodes SMPP Pdu objects into ByteBuf for transmission.
 *
 * <p>This encoder writes PDUs in the standard SMPP wire format with
 * length-prefixed frames.
 */
public class SmppPduEncoder extends MessageToByteEncoder<Pdu> {

    private static final Logger log = LoggerFactory.getLogger(SmppPduEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Pdu pdu, ByteBuf out) throws Exception {
        log.debug("Encoding PDU: {} seq={}", pdu.commandId(), pdu.sequenceNumber());

        // Write to a temporary buffer first to calculate length
        ByteBuf body = ctx.alloc().buffer();
        try {
            encodeBody(pdu, body);

            // Calculate total length
            int commandLength = Pdu.HEADER_SIZE + body.readableBytes();

            // Write header
            out.writeInt(commandLength);
            out.writeInt(pdu.commandId().code());
            out.writeInt(pdu.commandStatus().code());
            out.writeInt(pdu.sequenceNumber());

            // Write body
            out.writeBytes(body);
        } finally {
            body.release();
        }
    }

    private void encodeBody(Pdu pdu, ByteBuf buf) {
        switch (pdu) {
            case BindReceiver bind -> encodeBindRequest(buf, bind.systemId(), bind.password(),
                    bind.systemType(), bind.interfaceVersion(), bind.addrTon(),
                    bind.addrNpi(), bind.addressRange(), bind.optionalParameters());

            case BindReceiverResp resp -> encodeBindResponse(buf, resp.systemId(), resp.optionalParameters());

            case BindTransmitter bind -> encodeBindRequest(buf, bind.systemId(), bind.password(),
                    bind.systemType(), bind.interfaceVersion(), bind.addrTon(),
                    bind.addrNpi(), bind.addressRange(), bind.optionalParameters());

            case BindTransmitterResp resp -> encodeBindResponse(buf, resp.systemId(), resp.optionalParameters());

            case BindTransceiver bind -> encodeBindRequest(buf, bind.systemId(), bind.password(),
                    bind.systemType(), bind.interfaceVersion(), bind.addrTon(),
                    bind.addrNpi(), bind.addressRange(), bind.optionalParameters());

            case BindTransceiverResp resp -> encodeBindResponse(buf, resp.systemId(), resp.optionalParameters());

            case Unbind ignored -> { /* Empty body */ }
            case UnbindResp ignored -> { /* Empty body */ }

            case SubmitSm sm -> encodeSubmitSm(buf, sm);
            case SubmitSmResp resp -> encodeMessageIdResponse(buf, resp.messageId(), resp.optionalParameters());

            case DeliverSm sm -> encodeDeliverSm(buf, sm);
            case DeliverSmResp resp -> encodeMessageIdResponse(buf, resp.messageId(), resp.optionalParameters());

            case EnquireLink ignored -> { /* Empty body */ }
            case EnquireLinkResp ignored -> { /* Empty body */ }

            case GenericNack ignored -> { /* Empty body */ }

            case QuerySm query -> encodeQuerySm(buf, query);
            case QuerySmResp resp -> encodeQuerySmResp(buf, resp);

            case CancelSm cancel -> encodeCancelSm(buf, cancel);
            case CancelSmResp ignored -> { /* Empty body */ }

            case ReplaceSm replace -> encodeReplaceSm(buf, replace);
            case ReplaceSmResp ignored -> { /* Empty body */ }

            case DataSm data -> encodeDataSm(buf, data);
            case DataSmResp resp -> encodeMessageIdResponse(buf, resp.messageId(), resp.optionalParameters());

            case Outbind outbind -> encodeOutbind(buf, outbind);
            case AlertNotification alert -> encodeAlertNotification(buf, alert);

            case SubmitMulti multi -> encodeSubmitMulti(buf, multi);
            case SubmitMultiResp resp -> encodeSubmitMultiResp(buf, resp);
        }
    }

    private void encodeBindRequest(ByteBuf buf, String systemId, String password,
                                   String systemType, byte interfaceVersion,
                                   byte addrTon, byte addrNpi, String addressRange,
                                   List<Tlv> tlvs) {
        writeCString(buf, systemId);
        writeCString(buf, password);
        writeCString(buf, systemType);
        buf.writeByte(interfaceVersion);
        buf.writeByte(addrTon);
        buf.writeByte(addrNpi);
        writeCString(buf, addressRange);
        writeTlvs(buf, tlvs);
    }

    private void encodeBindResponse(ByteBuf buf, String systemId, List<Tlv> tlvs) {
        writeCString(buf, systemId);
        writeTlvs(buf, tlvs);
    }

    private void encodeSubmitSm(ByteBuf buf, SubmitSm sm) {
        writeCString(buf, sm.serviceType());
        writeAddress(buf, sm.sourceAddress());
        writeAddress(buf, sm.destAddress());
        buf.writeByte(sm.esmClass().toByte());
        buf.writeByte(sm.protocolId());
        buf.writeByte(sm.priorityFlag());
        writeCString(buf, sm.scheduleDeliveryTime());
        writeCString(buf, sm.validityPeriod());
        buf.writeByte(sm.registeredDelivery().toByte());
        buf.writeByte(sm.replaceIfPresent());
        buf.writeByte(sm.dataCoding().code());
        buf.writeByte(sm.smDefaultMsgId());
        buf.writeByte(sm.shortMessage().length);
        buf.writeBytes(sm.shortMessage());
        writeTlvs(buf, sm.optionalParameters());
    }

    private void encodeDeliverSm(ByteBuf buf, DeliverSm sm) {
        writeCString(buf, sm.serviceType());
        writeAddress(buf, sm.sourceAddress());
        writeAddress(buf, sm.destAddress());
        buf.writeByte(sm.esmClass().toByte());
        buf.writeByte(sm.protocolId());
        buf.writeByte(sm.priorityFlag());
        writeCString(buf, sm.scheduleDeliveryTime());
        writeCString(buf, sm.validityPeriod());
        buf.writeByte(sm.registeredDelivery().toByte());
        buf.writeByte(sm.replaceIfPresent());
        buf.writeByte(sm.dataCoding().code());
        buf.writeByte(sm.smDefaultMsgId());
        buf.writeByte(sm.shortMessage().length);
        buf.writeBytes(sm.shortMessage());
        writeTlvs(buf, sm.optionalParameters());
    }

    private void encodeQuerySm(ByteBuf buf, QuerySm query) {
        writeCString(buf, query.messageId());
        writeAddress(buf, query.sourceAddress());
        writeTlvs(buf, query.optionalParameters());
    }

    private void encodeQuerySmResp(ByteBuf buf, QuerySmResp resp) {
        writeCString(buf, resp.messageId());
        writeCString(buf, resp.finalDate());
        buf.writeByte(resp.messageState());
        buf.writeByte(resp.errorCode());
        writeTlvs(buf, resp.optionalParameters());
    }

    private void encodeCancelSm(ByteBuf buf, CancelSm cancel) {
        writeCString(buf, cancel.serviceType());
        writeCString(buf, cancel.messageId());
        writeAddress(buf, cancel.sourceAddress());
        writeAddress(buf, cancel.destAddress());
        writeTlvs(buf, cancel.optionalParameters());
    }

    private void encodeReplaceSm(ByteBuf buf, ReplaceSm replace) {
        writeCString(buf, replace.messageId());
        writeAddress(buf, replace.sourceAddress());
        writeCString(buf, replace.scheduleDeliveryTime());
        writeCString(buf, replace.validityPeriod());
        buf.writeByte(replace.registeredDelivery().toByte());
        buf.writeByte(replace.smDefaultMsgId());
        buf.writeByte(replace.shortMessage().length);
        buf.writeBytes(replace.shortMessage());
        writeTlvs(buf, replace.optionalParameters());
    }

    private void encodeDataSm(ByteBuf buf, DataSm data) {
        writeCString(buf, data.serviceType());
        writeAddress(buf, data.sourceAddress());
        writeAddress(buf, data.destAddress());
        buf.writeByte(data.esmClass().toByte());
        buf.writeByte(data.registeredDelivery().toByte());
        buf.writeByte(data.dataCoding().code());
        writeTlvs(buf, data.optionalParameters());
    }

    private void encodeOutbind(ByteBuf buf, Outbind outbind) {
        writeCString(buf, outbind.systemId());
        writeCString(buf, outbind.password());
    }

    private void encodeAlertNotification(ByteBuf buf, AlertNotification alert) {
        writeAddress(buf, alert.sourceAddress());
        writeAddress(buf, alert.esmeAddress());
        writeTlvs(buf, alert.optionalParameters());
    }

    private void encodeSubmitMulti(ByteBuf buf, SubmitMulti multi) {
        writeCString(buf, multi.serviceType());
        writeAddress(buf, multi.sourceAddress());

        // Write dest addresses
        buf.writeByte(multi.destAddresses().size());
        for (Address addr : multi.destAddresses()) {
            buf.writeByte(0x01); // SME address flag
            writeAddress(buf, addr);
        }

        buf.writeByte(multi.esmClass().toByte());
        buf.writeByte(multi.protocolId());
        buf.writeByte(multi.priorityFlag());
        writeCString(buf, multi.scheduleDeliveryTime());
        writeCString(buf, multi.validityPeriod());
        buf.writeByte(multi.registeredDelivery().toByte());
        buf.writeByte(multi.replaceIfPresent());
        buf.writeByte(multi.dataCoding().code());
        buf.writeByte(multi.smDefaultMsgId());
        buf.writeByte(multi.shortMessage().length);
        buf.writeBytes(multi.shortMessage());
        writeTlvs(buf, multi.optionalParameters());
    }

    private void encodeSubmitMultiResp(ByteBuf buf, SubmitMultiResp resp) {
        writeCString(buf, resp.messageId());
        buf.writeByte(resp.unsuccessfulDeliveries().size());
        for (SubmitMultiResp.UnsuccessfulDelivery ud : resp.unsuccessfulDeliveries()) {
            writeAddress(buf, ud.address());
            buf.writeInt(ud.errorStatus().code());
        }
        writeTlvs(buf, resp.optionalParameters());
    }

    private void encodeMessageIdResponse(ByteBuf buf, String messageId, List<Tlv> tlvs) {
        writeCString(buf, messageId);
        writeTlvs(buf, tlvs);
    }

    // Helper methods

    private void writeCString(ByteBuf buf, String value) {
        if (value != null && !value.isEmpty()) {
            buf.writeBytes(value.getBytes(StandardCharsets.ISO_8859_1));
        }
        buf.writeByte(0); // Null terminator
    }

    private void writeAddress(ByteBuf buf, Address address) {
        buf.writeByte(address.ton());
        buf.writeByte(address.npi());
        writeCString(buf, address.address());
    }

    private void writeTlvs(ByteBuf buf, List<Tlv> tlvs) {
        if (tlvs == null) return;
        for (Tlv tlv : tlvs) {
            buf.writeShort(tlv.rawTag());
            buf.writeShort(tlv.value().length);
            buf.writeBytes(tlv.value());
        }
    }
}
