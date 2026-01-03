package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.pdu.tlv.TlvTag;
import io.smppgateway.smpp.types.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DeliverSm Tests")
class DeliverSmTest {

    @Test
    @DisplayName("should create DeliverSm with required fields")
    void shouldCreateDeliverSmWithRequiredFields() {
        Address source = Address.international("+14155551234");
        Address dest = Address.international("+14155555678");

        DeliverSm deliverSm = new DeliverSm(
                1, "", source, dest, null, (byte) 0, (byte) 0,
                null, null, null, (byte) 0, null, (byte) 0,
                null, null
        );

        assertThat(deliverSm.sequenceNumber()).isEqualTo(1);
        assertThat(deliverSm.sourceAddress()).isEqualTo(source);
        assertThat(deliverSm.destAddress()).isEqualTo(dest);
        assertThat(deliverSm.commandId()).isEqualTo(CommandId.DELIVER_SM);
    }

    @Test
    @DisplayName("should use default values for null optional fields")
    void shouldUseDefaultValuesForNullFields() {
        Address source = Address.international("+14155551234");
        Address dest = Address.international("+14155555678");

        DeliverSm deliverSm = new DeliverSm(
                1, null, source, dest, null, (byte) 0, (byte) 0,
                null, null, null, (byte) 0, null, (byte) 0,
                null, null
        );

        assertThat(deliverSm.serviceType()).isEmpty();
        assertThat(deliverSm.esmClass()).isEqualTo(EsmClass.DEFAULT);
        assertThat(deliverSm.scheduleDeliveryTime()).isEmpty();
        assertThat(deliverSm.validityPeriod()).isEmpty();
        assertThat(deliverSm.registeredDelivery()).isEqualTo(RegisteredDelivery.NONE);
        assertThat(deliverSm.dataCoding()).isEqualTo(DataCoding.DEFAULT);
        assertThat(deliverSm.shortMessage()).isEmpty();
        assertThat(deliverSm.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should throw exception when sourceAddress is null")
    void shouldThrowExceptionWhenSourceAddressIsNull() {
        Address dest = Address.international("+14155555678");

        assertThatNullPointerException()
                .isThrownBy(() -> new DeliverSm(1, "", null, dest, null, (byte) 0, (byte) 0,
                        null, null, null, (byte) 0, null, (byte) 0, null, null))
                .withMessage("sourceAddress required");
    }

    @Test
    @DisplayName("should throw exception when destAddress is null")
    void shouldThrowExceptionWhenDestAddressIsNull() {
        Address source = Address.international("+14155551234");

        assertThatNullPointerException()
                .isThrownBy(() -> new DeliverSm(1, "", source, null, null, (byte) 0, (byte) 0,
                        null, null, null, (byte) 0, null, (byte) 0, null, null))
                .withMessage("destAddress required");
    }

    @Test
    @DisplayName("should identify as delivery receipt when ESM class indicates it")
    void shouldIdentifyAsDeliveryReceipt() {
        DeliverSm deliverSm = DeliverSm.builder()
                .sequenceNumber(1)
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .asDeliveryReceipt()
                .build();

        assertThat(deliverSm.isDeliveryReceipt()).isTrue();
    }

    @Test
    @DisplayName("should not identify as delivery receipt for normal message")
    void shouldNotIdentifyAsDeliveryReceiptForNormalMessage() {
        DeliverSm deliverSm = DeliverSm.builder()
                .sequenceNumber(1)
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .build();

        assertThat(deliverSm.isDeliveryReceipt()).isFalse();
    }

    @Test
    @DisplayName("should create defensive copy of short message")
    void shouldCreateDefensiveCopyOfShortMessage() {
        Address source = Address.international("+14155551234");
        Address dest = Address.international("+14155555678");
        byte[] originalMessage = "Hello".getBytes(StandardCharsets.UTF_8);

        DeliverSm deliverSm = new DeliverSm(
                1, "", source, dest, null, (byte) 0, (byte) 0,
                null, null, null, (byte) 0, null, (byte) 0,
                originalMessage, null
        );

        originalMessage[0] = (byte) 'X';

        assertThat(deliverSm.shortMessage()[0]).isEqualTo((byte) 'H');
    }

    @Test
    @DisplayName("should return defensive copy from shortMessage()")
    void shouldReturnDefensiveCopyFromShortMessage() {
        DeliverSm deliverSm = DeliverSm.builder()
                .sequenceNumber(1)
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .shortMessage("Hello".getBytes(StandardCharsets.UTF_8))
                .build();

        byte[] returned = deliverSm.shortMessage();
        returned[0] = (byte) 'X';

        assertThat(deliverSm.shortMessage()[0]).isEqualTo((byte) 'H');
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        DeliverSm deliverSm = DeliverSm.builder()
                .sequenceNumber(456)
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .build();

        DeliverSmResp response = deliverSm.createResponse();

        assertThat(response.sequenceNumber()).isEqualTo(456);
        assertThat(response.commandStatus()).isEqualTo(CommandStatus.OK);
    }

    @Test
    @DisplayName("should create response with specified status")
    void shouldCreateResponseWithSpecifiedStatus() {
        DeliverSm deliverSm = DeliverSm.builder()
                .sequenceNumber(456)
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .build();

        DeliverSmResp response = deliverSm.createResponse(CommandStatus.ESME_RX_T_APPN);

        assertThat(response.commandStatus()).isEqualTo(CommandStatus.ESME_RX_T_APPN);
    }

    // Builder tests

    @Test
    @DisplayName("builder should create DeliverSm with all fields")
    void builderShouldCreateDeliverSmWithAllFields() {
        byte[] message = "Incoming message".getBytes(StandardCharsets.UTF_8);

        DeliverSm deliverSm = DeliverSm.builder()
                .sequenceNumber(789)
                .serviceType("CMT")
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .esmClass(EsmClass.DEFAULT)
                .protocolId((byte) 0)
                .priorityFlag((byte) 0)
                .dataCoding(DataCoding.DEFAULT)
                .shortMessage(message)
                .addTlv(Tlv.ofString(TlvTag.RECEIPTED_MESSAGE_ID, "ORIG123"))
                .build();

        assertThat(deliverSm.sequenceNumber()).isEqualTo(789);
        assertThat(deliverSm.serviceType()).isEqualTo("CMT");
        assertThat(deliverSm.shortMessage()).isEqualTo(message);
        assertThat(deliverSm.optionalParameters()).hasSize(1);
    }

    @Test
    @DisplayName("builder should create delivery receipt using asDeliveryReceipt")
    void builderShouldCreateDeliveryReceiptUsingHelper() {
        DeliverSm deliverSm = DeliverSm.builder()
                .sequenceNumber(1)
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .asDeliveryReceipt()
                .shortMessage("id:123 sub:001 dlvrd:001 submit date:2101011200 done date:2101011201 stat:DELIVRD".getBytes())
                .build();

        assertThat(deliverSm.isDeliveryReceipt()).isTrue();
        assertThat(deliverSm.esmClass()).isEqualTo(EsmClass.DELIVERY_RECEIPT);
    }

    @Test
    @DisplayName("builder should support address components directly")
    void builderShouldSupportAddressComponentsDirectly() {
        DeliverSm deliverSm = DeliverSm.builder()
                .sequenceNumber(1)
                .sourceAddress(Address.TON_INTERNATIONAL, Address.NPI_E164, "+14155551234")
                .destAddress(Address.TON_ALPHANUMERIC, Address.NPI_UNKNOWN, "MYAPP")
                .build();

        assertThat(deliverSm.sourceAddress().ton()).isEqualTo(Address.TON_INTERNATIONAL);
        assertThat(deliverSm.destAddress().ton()).isEqualTo(Address.TON_ALPHANUMERIC);
        assertThat(deliverSm.destAddress().address()).isEqualTo("MYAPP");
    }
}
