package io.smppgateway.smpp.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.pdu.tlv.TlvTag;
import io.smppgateway.smpp.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Decodes SMPP PDU frames from ByteBuf into Pdu objects.
 *
 * <p>This decoder expects complete PDU frames as input (from SmppFrameDecoder).
 */
public class SmppPduDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(SmppPduDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf frame, List<Object> out) throws Exception {
        // Read header
        int commandLength = frame.readInt();
        int commandIdValue = frame.readInt();
        int commandStatusValue = frame.readInt();
        int sequenceNumber = frame.readInt();

        CommandId commandId = CommandId.fromCode(commandIdValue);
        CommandStatus commandStatus = CommandStatus.fromCode(commandStatusValue);

        if (commandId == null) {
            log.warn("Unknown command ID: 0x{}", Integer.toHexString(commandIdValue));
            throw new SmppDecodingException("Unknown command ID: " + commandIdValue);
        }

        log.debug("Decoding PDU: {} seq={} status={}", commandId, sequenceNumber, commandStatus);

        // Calculate body length (remaining bytes)
        int bodyLength = commandLength - Pdu.HEADER_SIZE;

        Pdu pdu = switch (commandId) {
            case BIND_RECEIVER -> decodeBindReceiver(frame, sequenceNumber, bodyLength);
            case BIND_RECEIVER_RESP -> decodeBindReceiverResp(frame, sequenceNumber, commandStatus, bodyLength);
            case BIND_TRANSMITTER -> decodeBindTransmitter(frame, sequenceNumber, bodyLength);
            case BIND_TRANSMITTER_RESP -> decodeBindTransmitterResp(frame, sequenceNumber, commandStatus, bodyLength);
            case BIND_TRANSCEIVER -> decodeBindTransceiver(frame, sequenceNumber, bodyLength);
            case BIND_TRANSCEIVER_RESP -> decodeBindTransceiverResp(frame, sequenceNumber, commandStatus, bodyLength);
            case UNBIND -> new Unbind(sequenceNumber);
            case UNBIND_RESP -> new UnbindResp(sequenceNumber, commandStatus);
            case SUBMIT_SM -> decodeSubmitSm(frame, sequenceNumber, bodyLength);
            case SUBMIT_SM_RESP -> decodeSubmitSmResp(frame, sequenceNumber, commandStatus, bodyLength);
            case DELIVER_SM -> decodeDeliverSm(frame, sequenceNumber, bodyLength);
            case DELIVER_SM_RESP -> decodeDeliverSmResp(frame, sequenceNumber, commandStatus, bodyLength);
            case ENQUIRE_LINK -> new EnquireLink(sequenceNumber);
            case ENQUIRE_LINK_RESP -> new EnquireLinkResp(sequenceNumber, commandStatus);
            case GENERIC_NACK -> new GenericNack(sequenceNumber, commandStatus);
            case QUERY_SM -> decodeQuerySm(frame, sequenceNumber, bodyLength);
            case QUERY_SM_RESP -> decodeQuerySmResp(frame, sequenceNumber, commandStatus, bodyLength);
            case CANCEL_SM -> decodeCancelSm(frame, sequenceNumber, bodyLength);
            case CANCEL_SM_RESP -> new CancelSmResp(sequenceNumber, commandStatus);
            case REPLACE_SM -> decodeReplaceSm(frame, sequenceNumber, bodyLength);
            case REPLACE_SM_RESP -> new ReplaceSmResp(sequenceNumber, commandStatus);
            case DATA_SM -> decodeDataSm(frame, sequenceNumber, bodyLength);
            case DATA_SM_RESP -> decodeDataSmResp(frame, sequenceNumber, commandStatus, bodyLength);
            case OUTBIND -> decodeOutbind(frame, sequenceNumber, bodyLength);
            case ALERT_NOTIFICATION -> decodeAlertNotification(frame, sequenceNumber, bodyLength);
            case SUBMIT_MULTI -> decodeSubmitMulti(frame, sequenceNumber, bodyLength);
            case SUBMIT_MULTI_RESP -> decodeSubmitMultiResp(frame, sequenceNumber, commandStatus, bodyLength);
        };

        out.add(pdu);
    }

    private BindReceiver decodeBindReceiver(ByteBuf buf, int seq, int bodyLen) {
        int startIdx = buf.readerIndex();
        String systemId = readCString(buf);
        String password = readCString(buf);
        String systemType = readCString(buf);
        byte interfaceVersion = buf.readByte();
        byte addrTon = buf.readByte();
        byte addrNpi = buf.readByte();
        String addressRange = readCString(buf);

        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));

        return new BindReceiver(seq, systemId, password, systemType,
                               interfaceVersion, addrTon, addrNpi, addressRange, tlvs);
    }

    private BindReceiverResp decodeBindReceiverResp(ByteBuf buf, int seq, CommandStatus status, int bodyLen) {
        if (bodyLen == 0 || status != CommandStatus.ESME_ROK) {
            return new BindReceiverResp(seq, status, "", List.of());
        }
        int startIdx = buf.readerIndex();
        String systemId = readCString(buf);
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new BindReceiverResp(seq, status, systemId, tlvs);
    }

    private BindTransmitter decodeBindTransmitter(ByteBuf buf, int seq, int bodyLen) {
        int startIdx = buf.readerIndex();
        String systemId = readCString(buf);
        String password = readCString(buf);
        String systemType = readCString(buf);
        byte interfaceVersion = buf.readByte();
        byte addrTon = buf.readByte();
        byte addrNpi = buf.readByte();
        String addressRange = readCString(buf);

        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));

        return new BindTransmitter(seq, systemId, password, systemType,
                                   interfaceVersion, addrTon, addrNpi, addressRange, tlvs);
    }

    private BindTransmitterResp decodeBindTransmitterResp(ByteBuf buf, int seq, CommandStatus status, int bodyLen) {
        if (bodyLen == 0 || status != CommandStatus.ESME_ROK) {
            return new BindTransmitterResp(seq, status, "", List.of());
        }
        int startIdx = buf.readerIndex();
        String systemId = readCString(buf);
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new BindTransmitterResp(seq, status, systemId, tlvs);
    }

    private BindTransceiver decodeBindTransceiver(ByteBuf buf, int seq, int bodyLen) {
        int startIdx = buf.readerIndex();
        String systemId = readCString(buf);
        String password = readCString(buf);
        String systemType = readCString(buf);
        byte interfaceVersion = buf.readByte();
        byte addrTon = buf.readByte();
        byte addrNpi = buf.readByte();
        String addressRange = readCString(buf);

        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));

        return new BindTransceiver(seq, systemId, password, systemType,
                                   interfaceVersion, addrTon, addrNpi, addressRange, tlvs);
    }

    private BindTransceiverResp decodeBindTransceiverResp(ByteBuf buf, int seq, CommandStatus status, int bodyLen) {
        if (bodyLen == 0 || status != CommandStatus.ESME_ROK) {
            return new BindTransceiverResp(seq, status, "", List.of());
        }
        int startIdx = buf.readerIndex();
        String systemId = readCString(buf);
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new BindTransceiverResp(seq, status, systemId, tlvs);
    }

    private SubmitSm decodeSubmitSm(ByteBuf buf, int seq, int bodyLen) {
        int startIdx = buf.readerIndex();

        String serviceType = readCString(buf);
        Address sourceAddr = readAddress(buf);
        Address destAddr = readAddress(buf);
        EsmClass esmClass = EsmClass.fromByte(buf.readByte());
        byte protocolId = buf.readByte();
        byte priorityFlag = buf.readByte();
        String scheduleDeliveryTime = readCString(buf);
        String validityPeriod = readCString(buf);
        RegisteredDelivery registeredDelivery = RegisteredDelivery.fromByte(buf.readByte());
        byte replaceIfPresent = buf.readByte();
        DataCoding dataCoding = DataCoding.fromByte(buf.readByte());
        byte smDefaultMsgId = buf.readByte();
        int smLength = buf.readUnsignedByte();
        byte[] shortMessage = new byte[smLength];
        buf.readBytes(shortMessage);

        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));

        return new SubmitSm(seq, serviceType, sourceAddr, destAddr, esmClass,
                           protocolId, priorityFlag, scheduleDeliveryTime, validityPeriod,
                           registeredDelivery, replaceIfPresent, dataCoding, smDefaultMsgId,
                           shortMessage, tlvs);
    }

    private SubmitSmResp decodeSubmitSmResp(ByteBuf buf, int seq, CommandStatus status, int bodyLen) {
        if (bodyLen == 0 || status != CommandStatus.ESME_ROK) {
            return new SubmitSmResp(seq, status, "", List.of());
        }
        int startIdx = buf.readerIndex();
        String messageId = readCString(buf);
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new SubmitSmResp(seq, status, messageId, tlvs);
    }

    private DeliverSm decodeDeliverSm(ByteBuf buf, int seq, int bodyLen) {
        int startIdx = buf.readerIndex();

        String serviceType = readCString(buf);
        Address sourceAddr = readAddress(buf);
        Address destAddr = readAddress(buf);
        EsmClass esmClass = EsmClass.fromByte(buf.readByte());
        byte protocolId = buf.readByte();
        byte priorityFlag = buf.readByte();
        String scheduleDeliveryTime = readCString(buf);
        String validityPeriod = readCString(buf);
        RegisteredDelivery registeredDelivery = RegisteredDelivery.fromByte(buf.readByte());
        byte replaceIfPresent = buf.readByte();
        DataCoding dataCoding = DataCoding.fromByte(buf.readByte());
        byte smDefaultMsgId = buf.readByte();
        int smLength = buf.readUnsignedByte();
        byte[] shortMessage = new byte[smLength];
        buf.readBytes(shortMessage);

        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));

        return new DeliverSm(seq, serviceType, sourceAddr, destAddr, esmClass,
                            protocolId, priorityFlag, scheduleDeliveryTime, validityPeriod,
                            registeredDelivery, replaceIfPresent, dataCoding, smDefaultMsgId,
                            shortMessage, tlvs);
    }

    private DeliverSmResp decodeDeliverSmResp(ByteBuf buf, int seq, CommandStatus status, int bodyLen) {
        if (bodyLen == 0) {
            return new DeliverSmResp(seq, status, "", List.of());
        }
        int startIdx = buf.readerIndex();
        String messageId = readCString(buf);
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new DeliverSmResp(seq, status, messageId, tlvs);
    }

    private QuerySm decodeQuerySm(ByteBuf buf, int seq, int bodyLen) {
        int startIdx = buf.readerIndex();
        String messageId = readCString(buf);
        Address sourceAddr = readAddress(buf);
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new QuerySm(seq, messageId, sourceAddr, tlvs);
    }

    private QuerySmResp decodeQuerySmResp(ByteBuf buf, int seq, CommandStatus status, int bodyLen) {
        if (bodyLen == 0 || status != CommandStatus.ESME_ROK) {
            return new QuerySmResp(seq, status, "", "", (byte) 0, (byte) 0, List.of());
        }
        int startIdx = buf.readerIndex();
        String messageId = readCString(buf);
        String finalDate = readCString(buf);
        byte messageState = buf.readByte();
        byte errorCode = buf.readByte();
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new QuerySmResp(seq, status, messageId, finalDate, messageState, errorCode, tlvs);
    }

    private CancelSm decodeCancelSm(ByteBuf buf, int seq, int bodyLen) {
        int startIdx = buf.readerIndex();
        String serviceType = readCString(buf);
        String messageId = readCString(buf);
        Address sourceAddr = readAddress(buf);
        Address destAddr = readAddress(buf);
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new CancelSm(seq, serviceType, messageId, sourceAddr, destAddr, tlvs);
    }

    private ReplaceSm decodeReplaceSm(ByteBuf buf, int seq, int bodyLen) {
        int startIdx = buf.readerIndex();
        String messageId = readCString(buf);
        Address sourceAddr = readAddress(buf);
        String scheduleDeliveryTime = readCString(buf);
        String validityPeriod = readCString(buf);
        RegisteredDelivery registeredDelivery = RegisteredDelivery.fromByte(buf.readByte());
        byte smDefaultMsgId = buf.readByte();
        int smLength = buf.readUnsignedByte();
        byte[] shortMessage = new byte[smLength];
        buf.readBytes(shortMessage);
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new ReplaceSm(seq, messageId, sourceAddr, scheduleDeliveryTime, validityPeriod,
                            registeredDelivery, smDefaultMsgId, shortMessage, tlvs);
    }

    private DataSm decodeDataSm(ByteBuf buf, int seq, int bodyLen) {
        int startIdx = buf.readerIndex();
        String serviceType = readCString(buf);
        Address sourceAddr = readAddress(buf);
        Address destAddr = readAddress(buf);
        EsmClass esmClass = EsmClass.fromByte(buf.readByte());
        RegisteredDelivery registeredDelivery = RegisteredDelivery.fromByte(buf.readByte());
        DataCoding dataCoding = DataCoding.fromByte(buf.readByte());
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new DataSm(seq, serviceType, sourceAddr, destAddr, esmClass,
                         registeredDelivery, dataCoding, tlvs);
    }

    private DataSmResp decodeDataSmResp(ByteBuf buf, int seq, CommandStatus status, int bodyLen) {
        if (bodyLen == 0 || status != CommandStatus.ESME_ROK) {
            return new DataSmResp(seq, status, "", List.of());
        }
        int startIdx = buf.readerIndex();
        String messageId = readCString(buf);
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new DataSmResp(seq, status, messageId, tlvs);
    }

    private Outbind decodeOutbind(ByteBuf buf, int seq, int bodyLen) {
        int startIdx = buf.readerIndex();
        String systemId = readCString(buf);
        String password = readCString(buf);
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new Outbind(seq, systemId, password, tlvs);
    }

    private AlertNotification decodeAlertNotification(ByteBuf buf, int seq, int bodyLen) {
        int startIdx = buf.readerIndex();
        Address sourceAddr = readAddress(buf);
        Address esmeAddr = readAddress(buf);
        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new AlertNotification(seq, sourceAddr, esmeAddr, tlvs);
    }

    private SubmitMulti decodeSubmitMulti(ByteBuf buf, int seq, int bodyLen) {
        int startIdx = buf.readerIndex();
        String serviceType = readCString(buf);
        Address sourceAddr = readAddress(buf);

        // Read dest addresses
        int numberOfDests = buf.readUnsignedByte();
        List<Address> destAddresses = new ArrayList<>(numberOfDests);
        for (int i = 0; i < numberOfDests; i++) {
            byte destFlag = buf.readByte();
            if (destFlag == 0x01) {
                // SME address
                destAddresses.add(readAddress(buf));
            } else if (destFlag == 0x02) {
                // Distribution list - read as c-string
                readCString(buf);
            }
        }

        EsmClass esmClass = EsmClass.fromByte(buf.readByte());
        byte protocolId = buf.readByte();
        byte priorityFlag = buf.readByte();
        String scheduleDeliveryTime = readCString(buf);
        String validityPeriod = readCString(buf);
        RegisteredDelivery registeredDelivery = RegisteredDelivery.fromByte(buf.readByte());
        byte replaceIfPresent = buf.readByte();
        DataCoding dataCoding = DataCoding.fromByte(buf.readByte());
        byte smDefaultMsgId = buf.readByte();
        int smLength = buf.readUnsignedByte();
        byte[] shortMessage = new byte[smLength];
        buf.readBytes(shortMessage);

        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));

        return new SubmitMulti(seq, serviceType, sourceAddr, destAddresses, esmClass,
                               protocolId, priorityFlag, scheduleDeliveryTime, validityPeriod,
                               registeredDelivery, replaceIfPresent, dataCoding, smDefaultMsgId,
                               shortMessage, tlvs);
    }

    private SubmitMultiResp decodeSubmitMultiResp(ByteBuf buf, int seq, CommandStatus status, int bodyLen) {
        if (bodyLen == 0 || status != CommandStatus.ESME_ROK) {
            return new SubmitMultiResp(seq, status, "", List.of(), List.of());
        }
        int startIdx = buf.readerIndex();
        String messageId = readCString(buf);

        // Read unsuccess SMEs
        int noUnsuccess = buf.readUnsignedByte();
        List<SubmitMultiResp.UnsuccessfulDelivery> unsuccessfuls = new ArrayList<>(noUnsuccess);
        for (int i = 0; i < noUnsuccess; i++) {
            Address addr = readAddress(buf);
            int errorCode = buf.readInt();
            unsuccessfuls.add(new SubmitMultiResp.UnsuccessfulDelivery(addr,
                              CommandStatus.fromCode(errorCode)));
        }

        List<Tlv> tlvs = readTlvs(buf, bodyLen - (buf.readerIndex() - startIdx));
        return new SubmitMultiResp(seq, status, messageId, unsuccessfuls, tlvs);
    }

    // Helper methods

    private String readCString(ByteBuf buf) {
        int start = buf.readerIndex();
        int nullPos = buf.indexOf(start, buf.writerIndex(), (byte) 0);
        if (nullPos < 0) {
            throw new SmppDecodingException("Missing null terminator in C-String");
        }
        int length = nullPos - start;
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        buf.skipBytes(1); // Skip null terminator
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    private Address readAddress(ByteBuf buf) {
        byte ton = buf.readByte();
        byte npi = buf.readByte();
        String address = readCString(buf);
        return new Address(ton, npi, address);
    }

    private List<Tlv> readTlvs(ByteBuf buf, int remainingBytes) {
        if (remainingBytes <= 0) {
            return List.of();
        }

        List<Tlv> tlvs = new ArrayList<>();
        int endIndex = buf.readerIndex() + remainingBytes;

        while (buf.readerIndex() < endIndex && buf.readableBytes() >= 4) {
            short tag = buf.readShort();
            int length = buf.readUnsignedShort();

            if (buf.readableBytes() < length) {
                throw new SmppDecodingException("TLV value length exceeds remaining bytes");
            }

            byte[] value = new byte[length];
            buf.readBytes(value);

            TlvTag tlvTag = TlvTag.fromCode(tag);
            // Use TlvTag constructor if known, otherwise use raw short tag
            tlvs.add(tlvTag != null ? new Tlv(tlvTag, value) : new Tlv(tag, value));
        }

        return tlvs;
    }
}
