package io.smppgateway.smpp.types;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RegisteredDelivery Tests")
class RegisteredDeliveryTest {

    @Test
    @DisplayName("should create NONE registered delivery")
    void shouldCreateNoneRegisteredDelivery() {
        RegisteredDelivery rd = RegisteredDelivery.NONE;

        assertThat(rd.value()).isEqualTo((byte) 0x00);
        assertThat(rd.isDeliveryReceiptRequested()).isFalse();
    }

    @Test
    @DisplayName("should create delivery receipt requested")
    void shouldCreateDeliveryReceiptRequested() {
        RegisteredDelivery rd = RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED;

        assertThat(rd.isDeliveryReceiptRequested()).isTrue();
        assertThat(rd.receiptType()).isEqualTo(RegisteredDelivery.RECEIPT_REQUESTED);
    }

    @Test
    @DisplayName("should create delivery receipt on failure")
    void shouldCreateDeliveryReceiptOnFailure() {
        RegisteredDelivery rd = RegisteredDelivery.SMSC_DELIVERY_RECEIPT_ON_FAILURE;

        assertThat(rd.isDeliveryReceiptRequested()).isTrue();
        assertThat(rd.receiptType()).isEqualTo(RegisteredDelivery.RECEIPT_ON_FAILURE);
    }

    @Test
    @DisplayName("should create delivery receipt on success")
    void shouldCreateDeliveryReceiptOnSuccess() {
        RegisteredDelivery rd = RegisteredDelivery.SMSC_DELIVERY_RECEIPT_ON_SUCCESS;

        assertThat(rd.isDeliveryReceiptRequested()).isTrue();
        assertThat(rd.receiptType()).isEqualTo(RegisteredDelivery.RECEIPT_ON_SUCCESS);
    }

    @Test
    @DisplayName("should correctly detect acknowledgement request")
    void shouldDetectAcknowledgementRequest() {
        RegisteredDelivery withAck = new RegisteredDelivery(RegisteredDelivery.ACK_DELIVERY);
        RegisteredDelivery withoutAck = RegisteredDelivery.NONE;

        assertThat(withAck.isAcknowledgementRequested()).isTrue();
        assertThat(withoutAck.isAcknowledgementRequested()).isFalse();
    }

    @Test
    @DisplayName("should correctly detect intermediate notification request")
    void shouldDetectIntermediateNotificationRequest() {
        RegisteredDelivery withNotification = new RegisteredDelivery(RegisteredDelivery.INTERMEDIATE_REQUESTED);
        RegisteredDelivery withoutNotification = RegisteredDelivery.NONE;

        assertThat(withNotification.isIntermediateNotificationRequested()).isTrue();
        assertThat(withoutNotification.isIntermediateNotificationRequested()).isFalse();
    }

    @Test
    @DisplayName("should add delivery receipt using fluent API")
    void shouldAddDeliveryReceiptWithFluentApi() {
        RegisteredDelivery rd = RegisteredDelivery.NONE.withDeliveryReceipt();

        assertThat(rd.isDeliveryReceiptRequested()).isTrue();
        assertThat(rd.receiptType()).isEqualTo(RegisteredDelivery.RECEIPT_REQUESTED);
    }

    @Test
    @DisplayName("should add delivery acknowledgement using fluent API")
    void shouldAddDeliveryAckWithFluentApi() {
        RegisteredDelivery rd = RegisteredDelivery.NONE.withDeliveryAck();

        assertThat(rd.isAcknowledgementRequested()).isTrue();
    }

    @Test
    @DisplayName("should add user acknowledgement using fluent API")
    void shouldAddUserAckWithFluentApi() {
        RegisteredDelivery rd = RegisteredDelivery.NONE.withUserAck();

        assertThat(rd.isAcknowledgementRequested()).isTrue();
    }

    @Test
    @DisplayName("should add intermediate notification using fluent API")
    void shouldAddIntermediateNotificationWithFluentApi() {
        RegisteredDelivery rd = RegisteredDelivery.NONE.withIntermediateNotification();

        assertThat(rd.isIntermediateNotificationRequested()).isTrue();
    }

    @Test
    @DisplayName("should chain fluent API methods")
    void shouldChainFluentApiMethods() {
        RegisteredDelivery rd = RegisteredDelivery.NONE
                .withDeliveryReceipt()
                .withDeliveryAck()
                .withIntermediateNotification();

        assertThat(rd.isDeliveryReceiptRequested()).isTrue();
        assertThat(rd.isAcknowledgementRequested()).isTrue();
        assertThat(rd.isIntermediateNotificationRequested()).isTrue();
    }

    @Test
    @DisplayName("should create from byte value")
    void shouldCreateFromByteValue() {
        RegisteredDelivery rd = RegisteredDelivery.of((byte) 0x15);

        assertThat(rd.value()).isEqualTo((byte) 0x15);
    }

    @Test
    @DisplayName("fromByte should work same as of")
    void fromByteShouldWorkSameAsOf() {
        byte value = (byte) 0x15;
        assertThat(RegisteredDelivery.fromByte(value)).isEqualTo(RegisteredDelivery.of(value));
    }

    @Test
    @DisplayName("should convert to byte")
    void shouldConvertToByte() {
        RegisteredDelivery rd = new RegisteredDelivery((byte) 0x15);

        assertThat(rd.toByte()).isEqualTo((byte) 0x15);
    }

    @Test
    @DisplayName("should have correct mask constants")
    void shouldHaveCorrectMaskConstants() {
        assertThat(RegisteredDelivery.RECEIPT_MASK).isEqualTo((byte) 0x03);
        assertThat(RegisteredDelivery.ACK_MASK).isEqualTo((byte) 0x0C);
        assertThat(RegisteredDelivery.INTERMEDIATE_MASK).isEqualTo((byte) 0x10);
    }

    @Test
    @DisplayName("should have correct receipt constants")
    void shouldHaveCorrectReceiptConstants() {
        assertThat(RegisteredDelivery.RECEIPT_NONE).isEqualTo((byte) 0x00);
        assertThat(RegisteredDelivery.RECEIPT_REQUESTED).isEqualTo((byte) 0x01);
        assertThat(RegisteredDelivery.RECEIPT_ON_FAILURE).isEqualTo((byte) 0x02);
        assertThat(RegisteredDelivery.RECEIPT_ON_SUCCESS).isEqualTo((byte) 0x03);
    }

    @Test
    @DisplayName("should have correct acknowledgement constants")
    void shouldHaveCorrectAckConstants() {
        assertThat(RegisteredDelivery.ACK_NONE).isEqualTo((byte) 0x00);
        assertThat(RegisteredDelivery.ACK_DELIVERY).isEqualTo((byte) 0x04);
        assertThat(RegisteredDelivery.ACK_USER).isEqualTo((byte) 0x08);
        assertThat(RegisteredDelivery.ACK_BOTH).isEqualTo((byte) 0x0C);
    }

    @Test
    @DisplayName("should preserve other bits when adding delivery receipt")
    void shouldPreserveOtherBitsWhenAddingReceipt() {
        RegisteredDelivery rd = new RegisteredDelivery((byte) (RegisteredDelivery.ACK_DELIVERY | RegisteredDelivery.INTERMEDIATE_REQUESTED))
                .withDeliveryReceipt();

        assertThat(rd.isDeliveryReceiptRequested()).isTrue();
        assertThat(rd.isAcknowledgementRequested()).isTrue();
        assertThat(rd.isIntermediateNotificationRequested()).isTrue();
    }
}
