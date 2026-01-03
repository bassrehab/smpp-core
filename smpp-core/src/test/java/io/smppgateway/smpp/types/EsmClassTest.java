package io.smppgateway.smpp.types;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EsmClass Tests")
class EsmClassTest {

    @Test
    @DisplayName("should create default ESM class")
    void shouldCreateDefaultEsmClass() {
        EsmClass esmClass = EsmClass.DEFAULT;

        assertThat(esmClass.value()).isEqualTo((byte) 0x00);
        assertThat(esmClass.mode()).isEqualTo(EsmClass.MODE_DEFAULT);
        assertThat(esmClass.messageType()).isEqualTo(EsmClass.TYPE_DEFAULT);
        assertThat(esmClass.gsmFeatures()).isEqualTo(EsmClass.GSM_NO_FEATURES);
    }

    @Test
    @DisplayName("should create delivery receipt ESM class")
    void shouldCreateDeliveryReceiptEsmClass() {
        EsmClass esmClass = EsmClass.DELIVERY_RECEIPT;

        assertThat(esmClass.isDeliveryReceipt()).isTrue();
        assertThat(esmClass.messageType()).isEqualTo(EsmClass.TYPE_DELIVERY_RECEIPT);
    }

    @Test
    @DisplayName("should correctly identify delivery receipt")
    void shouldIdentifyDeliveryReceipt() {
        EsmClass deliveryReceipt = new EsmClass(EsmClass.TYPE_DELIVERY_RECEIPT);
        EsmClass normalMessage = new EsmClass((byte) 0x00);

        assertThat(deliveryReceipt.isDeliveryReceipt()).isTrue();
        assertThat(normalMessage.isDeliveryReceipt()).isFalse();
    }

    @Test
    @DisplayName("should correctly extract messaging mode")
    void shouldExtractMessagingMode() {
        assertThat(new EsmClass(EsmClass.MODE_DEFAULT).mode()).isEqualTo(EsmClass.MODE_DEFAULT);
        assertThat(new EsmClass(EsmClass.MODE_DATAGRAM).mode()).isEqualTo(EsmClass.MODE_DATAGRAM);
        assertThat(new EsmClass(EsmClass.MODE_FORWARD).mode()).isEqualTo(EsmClass.MODE_FORWARD);
        assertThat(new EsmClass(EsmClass.MODE_STORE_AND_FORWARD).mode()).isEqualTo(EsmClass.MODE_STORE_AND_FORWARD);
    }

    @Test
    @DisplayName("should correctly extract message type")
    void shouldExtractMessageType() {
        assertThat(new EsmClass(EsmClass.TYPE_DEFAULT).messageType()).isEqualTo(EsmClass.TYPE_DEFAULT);
        assertThat(new EsmClass(EsmClass.TYPE_DELIVERY_RECEIPT).messageType()).isEqualTo(EsmClass.TYPE_DELIVERY_RECEIPT);
        assertThat(new EsmClass(EsmClass.TYPE_DELIVERY_ACK).messageType()).isEqualTo(EsmClass.TYPE_DELIVERY_ACK);
        assertThat(new EsmClass(EsmClass.TYPE_USER_ACK).messageType()).isEqualTo(EsmClass.TYPE_USER_ACK);
    }

    @Test
    @DisplayName("should correctly detect UDHI flag")
    void shouldDetectUdhiFlag() {
        EsmClass withUdhi = new EsmClass(EsmClass.GSM_UDHI);
        EsmClass withoutUdhi = new EsmClass((byte) 0x00);

        assertThat(withUdhi.hasUdhi()).isTrue();
        assertThat(withoutUdhi.hasUdhi()).isFalse();
    }

    @Test
    @DisplayName("should correctly detect reply path flag")
    void shouldDetectReplyPathFlag() {
        EsmClass withReplyPath = new EsmClass(EsmClass.GSM_REPLY_PATH);
        EsmClass withoutReplyPath = new EsmClass((byte) 0x00);

        assertThat(withReplyPath.hasReplyPath()).isTrue();
        assertThat(withoutReplyPath.hasReplyPath()).isFalse();
    }

    @Test
    @DisplayName("should create ESM class with mode using fluent API")
    void shouldCreateWithMode() {
        EsmClass esmClass = EsmClass.DEFAULT.withMode(EsmClass.MODE_DATAGRAM);

        assertThat(esmClass.mode()).isEqualTo(EsmClass.MODE_DATAGRAM);
    }

    @Test
    @DisplayName("should create ESM class with message type using fluent API")
    void shouldCreateWithMessageType() {
        EsmClass esmClass = EsmClass.DEFAULT.withMessageType(EsmClass.TYPE_DELIVERY_RECEIPT);

        assertThat(esmClass.isDeliveryReceipt()).isTrue();
    }

    @Test
    @DisplayName("should create ESM class with UDHI using fluent API")
    void shouldCreateWithUdhi() {
        EsmClass esmClass = EsmClass.DEFAULT.withUdhi();

        assertThat(esmClass.hasUdhi()).isTrue();
    }

    @Test
    @DisplayName("should create ESM class with reply path using fluent API")
    void shouldCreateWithReplyPath() {
        EsmClass esmClass = EsmClass.DEFAULT.withReplyPath();

        assertThat(esmClass.hasReplyPath()).isTrue();
    }

    @Test
    @DisplayName("should chain fluent API methods")
    void shouldChainFluentApiMethods() {
        EsmClass esmClass = EsmClass.DEFAULT
                .withMode(EsmClass.MODE_STORE_AND_FORWARD)
                .withUdhi()
                .withReplyPath();

        assertThat(esmClass.mode()).isEqualTo(EsmClass.MODE_STORE_AND_FORWARD);
        assertThat(esmClass.hasUdhi()).isTrue();
        assertThat(esmClass.hasReplyPath()).isTrue();
    }

    @Test
    @DisplayName("should preserve other bits when setting mode")
    void shouldPreserveOtherBitsWhenSettingMode() {
        EsmClass esmClass = new EsmClass((byte) (EsmClass.GSM_UDHI | EsmClass.TYPE_DELIVERY_RECEIPT))
                .withMode(EsmClass.MODE_DATAGRAM);

        assertThat(esmClass.mode()).isEqualTo(EsmClass.MODE_DATAGRAM);
        assertThat(esmClass.hasUdhi()).isTrue();
        assertThat(esmClass.isDeliveryReceipt()).isTrue();
    }

    @Test
    @DisplayName("should preserve other bits when setting message type")
    void shouldPreserveOtherBitsWhenSettingMessageType() {
        EsmClass esmClass = new EsmClass((byte) (EsmClass.GSM_UDHI | EsmClass.MODE_DATAGRAM))
                .withMessageType(EsmClass.TYPE_DELIVERY_RECEIPT);

        assertThat(esmClass.mode()).isEqualTo(EsmClass.MODE_DATAGRAM);
        assertThat(esmClass.hasUdhi()).isTrue();
        assertThat(esmClass.isDeliveryReceipt()).isTrue();
    }

    @Test
    @DisplayName("should create from byte value")
    void shouldCreateFromByteValue() {
        byte value = (byte) 0x45; // Some combination
        EsmClass esmClass = EsmClass.of(value);

        assertThat(esmClass.value()).isEqualTo(value);
    }

    @Test
    @DisplayName("should create from byte using fromByte method")
    void shouldCreateFromByte() {
        byte value = (byte) 0x45;
        EsmClass esmClass = EsmClass.fromByte(value);

        assertThat(esmClass.value()).isEqualTo(value);
    }

    @Test
    @DisplayName("should convert to byte")
    void shouldConvertToByte() {
        EsmClass esmClass = new EsmClass((byte) 0x45);

        assertThat(esmClass.toByte()).isEqualTo((byte) 0x45);
    }

    @Test
    @DisplayName("should have correct mode mask constants")
    void shouldHaveCorrectModeMaskConstants() {
        assertThat(EsmClass.MODE_MASK).isEqualTo((byte) 0x03);
        assertThat(EsmClass.MODE_DEFAULT).isEqualTo((byte) 0x00);
        assertThat(EsmClass.MODE_DATAGRAM).isEqualTo((byte) 0x01);
        assertThat(EsmClass.MODE_FORWARD).isEqualTo((byte) 0x02);
        assertThat(EsmClass.MODE_STORE_AND_FORWARD).isEqualTo((byte) 0x03);
    }

    @Test
    @DisplayName("should have correct type mask constants")
    void shouldHaveCorrectTypeMaskConstants() {
        assertThat(EsmClass.TYPE_MASK).isEqualTo((byte) 0x3C);
        assertThat(EsmClass.TYPE_DEFAULT).isEqualTo((byte) 0x00);
        assertThat(EsmClass.TYPE_DELIVERY_RECEIPT).isEqualTo((byte) 0x04);
        assertThat(EsmClass.TYPE_DELIVERY_ACK).isEqualTo((byte) 0x08);
        assertThat(EsmClass.TYPE_USER_ACK).isEqualTo((byte) 0x10);
        assertThat(EsmClass.TYPE_CONVERSATION_ABORT).isEqualTo((byte) 0x18);
        assertThat(EsmClass.TYPE_INTERMEDIATE_NOTIFICATION).isEqualTo((byte) 0x20);
    }

    @Test
    @DisplayName("should have correct GSM feature constants")
    void shouldHaveCorrectGsmFeatureConstants() {
        assertThat(EsmClass.GSM_MASK).isEqualTo((byte) 0xC0);
        assertThat(EsmClass.GSM_NO_FEATURES).isEqualTo((byte) 0x00);
        assertThat(EsmClass.GSM_UDHI).isEqualTo((byte) 0x40);
        assertThat(EsmClass.GSM_REPLY_PATH).isEqualTo((byte) 0x80);
        assertThat(EsmClass.GSM_UDHI_AND_REPLY).isEqualTo((byte) 0xC0);
    }
}
