package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.pdu.tlv.TlvTag;
import io.smppgateway.smpp.types.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SubmitSm Tests")
class SubmitSmTest {

    @Test
    @DisplayName("should create SubmitSm with required fields")
    void shouldCreateSubmitSmWithRequiredFields() {
        Address source = Address.international("+14155551234");
        Address dest = Address.international("+14155555678");

        SubmitSm submitSm = new SubmitSm(
                1, "", source, dest, null, (byte) 0, (byte) 0,
                null, null, null, (byte) 0, null, (byte) 0,
                null, null
        );

        assertThat(submitSm.sequenceNumber()).isEqualTo(1);
        assertThat(submitSm.sourceAddress()).isEqualTo(source);
        assertThat(submitSm.destAddress()).isEqualTo(dest);
        assertThat(submitSm.commandId()).isEqualTo(CommandId.SUBMIT_SM);
    }

    @Test
    @DisplayName("should use default values for null optional fields")
    void shouldUseDefaultValuesForNullFields() {
        Address source = Address.international("+14155551234");
        Address dest = Address.international("+14155555678");

        SubmitSm submitSm = new SubmitSm(
                1, null, source, dest, null, (byte) 0, (byte) 0,
                null, null, null, (byte) 0, null, (byte) 0,
                null, null
        );

        assertThat(submitSm.serviceType()).isEmpty();
        assertThat(submitSm.esmClass()).isEqualTo(EsmClass.DEFAULT);
        assertThat(submitSm.scheduleDeliveryTime()).isEmpty();
        assertThat(submitSm.validityPeriod()).isEmpty();
        assertThat(submitSm.registeredDelivery()).isEqualTo(RegisteredDelivery.NONE);
        assertThat(submitSm.dataCoding()).isEqualTo(DataCoding.DEFAULT);
        assertThat(submitSm.shortMessage()).isEmpty();
        assertThat(submitSm.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should throw exception when sourceAddress is null")
    void shouldThrowExceptionWhenSourceAddressIsNull() {
        Address dest = Address.international("+14155555678");

        assertThatNullPointerException()
                .isThrownBy(() -> new SubmitSm(1, "", null, dest, null, (byte) 0, (byte) 0,
                        null, null, null, (byte) 0, null, (byte) 0, null, null))
                .withMessage("sourceAddress required");
    }

    @Test
    @DisplayName("should throw exception when destAddress is null")
    void shouldThrowExceptionWhenDestAddressIsNull() {
        Address source = Address.international("+14155551234");

        assertThatNullPointerException()
                .isThrownBy(() -> new SubmitSm(1, "", source, null, null, (byte) 0, (byte) 0,
                        null, null, null, (byte) 0, null, (byte) 0, null, null))
                .withMessage("destAddress required");
    }

    @Test
    @DisplayName("should create defensive copy of short message")
    void shouldCreateDefensiveCopyOfShortMessage() {
        Address source = Address.international("+14155551234");
        Address dest = Address.international("+14155555678");
        byte[] originalMessage = "Hello".getBytes(StandardCharsets.UTF_8);

        SubmitSm submitSm = new SubmitSm(
                1, "", source, dest, null, (byte) 0, (byte) 0,
                null, null, null, (byte) 0, null, (byte) 0,
                originalMessage, null
        );

        // Modify original array
        originalMessage[0] = (byte) 'X';

        // SubmitSm should still have original value
        assertThat(submitSm.shortMessage()[0]).isEqualTo((byte) 'H');
    }

    @Test
    @DisplayName("should return defensive copy from shortMessage()")
    void shouldReturnDefensiveCopyFromShortMessage() {
        Address source = Address.international("+14155551234");
        Address dest = Address.international("+14155555678");
        byte[] message = "Hello".getBytes(StandardCharsets.UTF_8);

        SubmitSm submitSm = new SubmitSm(
                1, "", source, dest, null, (byte) 0, (byte) 0,
                null, null, null, (byte) 0, null, (byte) 0,
                message, null
        );

        byte[] returned = submitSm.shortMessage();
        returned[0] = (byte) 'X';

        // Original should be unchanged
        assertThat(submitSm.shortMessage()[0]).isEqualTo((byte) 'H');
    }

    @Test
    @DisplayName("should return correct short message length")
    void shouldReturnCorrectShortMessageLength() {
        Address source = Address.international("+14155551234");
        Address dest = Address.international("+14155555678");
        byte[] message = "Hello World".getBytes(StandardCharsets.UTF_8);

        SubmitSm submitSm = new SubmitSm(
                1, "", source, dest, null, (byte) 0, (byte) 0,
                null, null, null, (byte) 0, null, (byte) 0,
                message, null
        );

        assertThat(submitSm.shortMessageLength()).isEqualTo(11);
    }

    @Test
    @DisplayName("should create immutable copy of optional parameters list")
    void shouldCreateImmutableCopyOfOptionalParameters() {
        Address source = Address.international("+14155551234");
        Address dest = Address.international("+14155555678");
        List<Tlv> tlvs = List.of(Tlv.ofByte(TlvTag.MESSAGE_STATE, (byte) 0x02));

        SubmitSm submitSm = new SubmitSm(
                1, "", source, dest, null, (byte) 0, (byte) 0,
                null, null, null, (byte) 0, null, (byte) 0,
                null, tlvs
        );

        assertThat(submitSm.optionalParameters()).hasSize(1);
        assertThatThrownBy(() -> submitSm.optionalParameters().add(
                Tlv.ofByte(TlvTag.MESSAGE_STATE, (byte) 0x01)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        SubmitSm submitSm = SubmitSm.builder()
                .sequenceNumber(123)
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .build();

        SubmitSmResp response = submitSm.createResponse();

        assertThat(response.sequenceNumber()).isEqualTo(123);
        assertThat(response.commandStatus()).isEqualTo(CommandStatus.OK);
    }

    @Test
    @DisplayName("should create response with specified status")
    void shouldCreateResponseWithSpecifiedStatus() {
        SubmitSm submitSm = SubmitSm.builder()
                .sequenceNumber(123)
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .build();

        SubmitSmResp response = submitSm.createResponse(CommandStatus.ESME_RTHROTTLED);

        assertThat(response.sequenceNumber()).isEqualTo(123);
        assertThat(response.commandStatus()).isEqualTo(CommandStatus.ESME_RTHROTTLED);
    }

    @Test
    @DisplayName("should create response with message ID")
    void shouldCreateResponseWithMessageId() {
        SubmitSm submitSm = SubmitSm.builder()
                .sequenceNumber(123)
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .build();

        SubmitSmResp response = submitSm.createResponse(CommandStatus.OK, "MSG123456");

        assertThat(response.messageId()).isEqualTo("MSG123456");
    }

    // Builder tests

    @Test
    @DisplayName("builder should create SubmitSm with all fields")
    void builderShouldCreateSubmitSmWithAllFields() {
        byte[] message = "Hello".getBytes(StandardCharsets.UTF_8);

        SubmitSm submitSm = SubmitSm.builder()
                .sequenceNumber(42)
                .serviceType("CMT")
                .sourceAddress(Address.alphanumeric("MYCOMPANY"))
                .destAddress(Address.international("+14155555678"))
                .esmClass(EsmClass.DEFAULT.withUdhi())
                .protocolId((byte) 0)
                .priorityFlag((byte) 1)
                .scheduleDeliveryTime("210101120000000R")
                .validityPeriod("000000120000000R")
                .registeredDelivery(RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED)
                .replaceIfPresent((byte) 0)
                .dataCoding(DataCoding.UCS2)
                .smDefaultMsgId((byte) 0)
                .shortMessage(message)
                .addTlv(Tlv.ofShort(TlvTag.SAR_MSG_REF_NUM, (short) 1))
                .build();

        assertThat(submitSm.sequenceNumber()).isEqualTo(42);
        assertThat(submitSm.serviceType()).isEqualTo("CMT");
        assertThat(submitSm.sourceAddress().address()).isEqualTo("MYCOMPANY");
        assertThat(submitSm.destAddress().address()).isEqualTo("+14155555678");
        assertThat(submitSm.esmClass().hasUdhi()).isTrue();
        assertThat(submitSm.priorityFlag()).isEqualTo((byte) 1);
        assertThat(submitSm.scheduleDeliveryTime()).isEqualTo("210101120000000R");
        assertThat(submitSm.validityPeriod()).isEqualTo("000000120000000R");
        assertThat(submitSm.registeredDelivery().isDeliveryReceiptRequested()).isTrue();
        assertThat(submitSm.dataCoding()).isEqualTo(DataCoding.UCS2);
        assertThat(submitSm.shortMessage()).isEqualTo(message);
        assertThat(submitSm.optionalParameters()).hasSize(1);
    }

    @Test
    @DisplayName("builder should use address components directly")
    void builderShouldUseAddressComponentsDirectly() {
        SubmitSm submitSm = SubmitSm.builder()
                .sequenceNumber(1)
                .sourceAddress(Address.TON_ALPHANUMERIC, Address.NPI_UNKNOWN, "SENDER")
                .destAddress(Address.TON_INTERNATIONAL, Address.NPI_E164, "+14155555678")
                .build();

        assertThat(submitSm.sourceAddress().ton()).isEqualTo(Address.TON_ALPHANUMERIC);
        assertThat(submitSm.sourceAddress().address()).isEqualTo("SENDER");
        assertThat(submitSm.destAddress().ton()).isEqualTo(Address.TON_INTERNATIONAL);
    }

    @Test
    @DisplayName("builder requestDeliveryReceipt should set registered delivery")
    void builderRequestDeliveryReceiptShouldSetRegisteredDelivery() {
        SubmitSm submitSm = SubmitSm.builder()
                .sequenceNumber(1)
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .requestDeliveryReceipt()
                .build();

        assertThat(submitSm.registeredDelivery())
                .isEqualTo(RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED);
    }

    @Test
    @DisplayName("should have correct MAX_SHORT_MESSAGE_LENGTH constant")
    void shouldHaveCorrectMaxShortMessageLength() {
        assertThat(SubmitSm.MAX_SHORT_MESSAGE_LENGTH).isEqualTo(254);
    }
}
