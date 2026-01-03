package io.smppgateway.smpp.pdu.tlv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tlv Tests")
class TlvTest {

    @Test
    @DisplayName("should create TLV with tag and value")
    void shouldCreateTlvWithTagAndValue() {
        byte[] value = {0x01, 0x02, 0x03};
        Tlv tlv = new Tlv((short) 0x0005, value);

        assertThat(tlv.tag()).isEqualTo((short) 0x0005);
        assertThat(tlv.value()).isEqualTo(value);
        assertThat(tlv.length()).isEqualTo(3);
    }

    @Test
    @DisplayName("should create TLV with TlvTag enum")
    void shouldCreateTlvWithTlvTagEnum() {
        byte[] value = {0x01, 0x02};
        Tlv tlv = new Tlv(TlvTag.USER_MESSAGE_REFERENCE, value);

        assertThat(tlv.tag()).isEqualTo(TlvTag.USER_MESSAGE_REFERENCE.code());
        assertThat(tlv.value()).isEqualTo(value);
    }

    @Test
    @DisplayName("should throw exception when value is null")
    void shouldThrowExceptionWhenValueIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Tlv((short) 0x0005, null))
                .withMessage("value must not be null");
    }

    @Test
    @DisplayName("should create defensive copy of value")
    void shouldCreateDefensiveCopyOfValue() {
        byte[] originalValue = {0x01, 0x02, 0x03};
        Tlv tlv = new Tlv((short) 0x0005, originalValue);

        // Modify original array
        originalValue[0] = (byte) 0xFF;

        // TLV should still have original value
        assertThat(tlv.value()[0]).isEqualTo((byte) 0x01);
    }

    @Test
    @DisplayName("should return defensive copy from value() method")
    void shouldReturnDefensiveCopyFromValueMethod() {
        byte[] value = {0x01, 0x02, 0x03};
        Tlv tlv = new Tlv((short) 0x0005, value);

        byte[] returned = tlv.value();
        returned[0] = (byte) 0xFF;

        // Original TLV should be unchanged
        assertThat(tlv.value()[0]).isEqualTo((byte) 0x01);
    }

    @Test
    @DisplayName("should return raw tag")
    void shouldReturnRawTag() {
        Tlv tlv = new Tlv((short) 0x0424, new byte[]{0x01});

        assertThat(tlv.rawTag()).isEqualTo((short) 0x0424);
    }

    @Test
    @DisplayName("should convert value to string")
    void shouldConvertValueToString() {
        byte[] value = "Hello\0".getBytes(StandardCharsets.ISO_8859_1);
        Tlv tlv = new Tlv(TlvTag.RECEIPTED_MESSAGE_ID, value);

        assertThat(tlv.valueAsString()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("should convert value to string without null terminator")
    void shouldConvertValueToStringWithoutNullTerminator() {
        byte[] value = "Hello".getBytes(StandardCharsets.ISO_8859_1);
        Tlv tlv = new Tlv(TlvTag.RECEIPTED_MESSAGE_ID, value);

        assertThat(tlv.valueAsString()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("should convert value to byte")
    void shouldConvertValueToByte() {
        Tlv tlv = new Tlv(TlvTag.MESSAGE_STATE, new byte[]{0x02});

        assertThat(tlv.valueAsByte()).isEqualTo(0x02);
    }

    @Test
    @DisplayName("should throw exception when converting empty value to byte")
    void shouldThrowExceptionForEmptyValueAsByte() {
        Tlv tlv = new Tlv((short) 0x0001, new byte[0]);

        assertThatIllegalStateException()
                .isThrownBy(tlv::valueAsByte)
                .withMessage("TLV value is empty");
    }

    @Test
    @DisplayName("should convert value to short (big-endian)")
    void shouldConvertValueToShort() {
        Tlv tlv = new Tlv(TlvTag.SAR_MSG_REF_NUM, new byte[]{0x01, 0x02});

        assertThat(tlv.valueAsShort()).isEqualTo(0x0102);
    }

    @Test
    @DisplayName("should throw exception when value too short for short")
    void shouldThrowExceptionForShortValueAsShort() {
        Tlv tlv = new Tlv((short) 0x0001, new byte[]{0x01});

        assertThatIllegalStateException()
                .isThrownBy(tlv::valueAsShort)
                .withMessage("TLV value too short for short");
    }

    @Test
    @DisplayName("should convert value to int (big-endian)")
    void shouldConvertValueToInt() {
        Tlv tlv = new Tlv(TlvTag.QOS_TIME_TO_LIVE, new byte[]{0x00, 0x01, 0x51, (byte) 0x80});

        assertThat(tlv.valueAsInt()).isEqualTo(86400L); // 24 hours in seconds
    }

    @Test
    @DisplayName("should throw exception when value too short for int")
    void shouldThrowExceptionForShortValueAsInt() {
        Tlv tlv = new Tlv((short) 0x0001, new byte[]{0x01, 0x02, 0x03});

        assertThatIllegalStateException()
                .isThrownBy(tlv::valueAsInt)
                .withMessage("TLV value too short for int");
    }

    @Test
    @DisplayName("should return known tag")
    void shouldReturnKnownTag() {
        Tlv tlv = new Tlv(TlvTag.MESSAGE_PAYLOAD, new byte[]{0x01});

        assertThat(tlv.knownTag()).isEqualTo(TlvTag.MESSAGE_PAYLOAD);
    }

    @Test
    @DisplayName("should return null for unknown tag")
    void shouldReturnNullForUnknownTag() {
        Tlv tlv = new Tlv((short) 0x9999, new byte[]{0x01});

        assertThat(tlv.knownTag()).isNull();
    }

    @Test
    @DisplayName("should create TLV with string value using factory method")
    void shouldCreateTlvWithStringValue() {
        Tlv tlv = Tlv.ofString(TlvTag.RECEIPTED_MESSAGE_ID, "MSG123");

        assertThat(tlv.valueAsString()).isEqualTo("MSG123");
        // Check that null terminator is added
        assertThat(tlv.value()[tlv.value().length - 1]).isEqualTo((byte) 0);
    }

    @Test
    @DisplayName("should create TLV with byte value using factory method")
    void shouldCreateTlvWithByteValue() {
        Tlv tlv = Tlv.ofByte(TlvTag.MESSAGE_STATE, (byte) 0x02);

        assertThat(tlv.valueAsByte()).isEqualTo(0x02);
        assertThat(tlv.length()).isEqualTo(1);
    }

    @Test
    @DisplayName("should create TLV with short value using factory method")
    void shouldCreateTlvWithShortValue() {
        Tlv tlv = Tlv.ofShort(TlvTag.SAR_MSG_REF_NUM, (short) 0x0102);

        assertThat(tlv.valueAsShort()).isEqualTo(0x0102);
        assertThat(tlv.length()).isEqualTo(2);
    }

    @Test
    @DisplayName("should create TLV with int value using factory method")
    void shouldCreateTlvWithIntValue() {
        Tlv tlv = Tlv.ofInt(TlvTag.QOS_TIME_TO_LIVE, 86400);

        assertThat(tlv.valueAsInt()).isEqualTo(86400L);
        assertThat(tlv.length()).isEqualTo(4);
    }

    @Test
    @DisplayName("should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        Tlv tlv1 = new Tlv((short) 0x0005, new byte[]{0x01, 0x02});
        Tlv tlv2 = new Tlv((short) 0x0005, new byte[]{0x01, 0x02});
        Tlv tlv3 = new Tlv((short) 0x0005, new byte[]{0x01, 0x03});
        Tlv tlv4 = new Tlv((short) 0x0006, new byte[]{0x01, 0x02});

        assertThat(tlv1).isEqualTo(tlv2);
        assertThat(tlv1).isNotEqualTo(tlv3);
        assertThat(tlv1).isNotEqualTo(tlv4);
    }

    @Test
    @DisplayName("should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        Tlv tlv1 = new Tlv((short) 0x0005, new byte[]{0x01, 0x02});
        Tlv tlv2 = new Tlv((short) 0x0005, new byte[]{0x01, 0x02});

        assertThat(tlv1.hashCode()).isEqualTo(tlv2.hashCode());
    }

    @Test
    @DisplayName("should have meaningful toString for known tag")
    void shouldHaveMeaningfulToStringForKnownTag() {
        Tlv tlv = new Tlv(TlvTag.MESSAGE_PAYLOAD, new byte[]{0x01, 0x02, 0x03});

        String result = tlv.toString();

        assertThat(result).contains("MESSAGE_PAYLOAD");
        assertThat(result).contains("length=3");
    }

    @Test
    @DisplayName("should have meaningful toString for unknown tag")
    void shouldHaveMeaningfulToStringForUnknownTag() {
        Tlv tlv = new Tlv((short) 0x9999, new byte[]{0x01});

        String result = tlv.toString();

        assertThat(result).contains("0x9999");
    }

    @Test
    @DisplayName("should handle maximum value length")
    void shouldHandleMaxValueLength() {
        byte[] maxValue = new byte[Tlv.MAX_VALUE_LENGTH];
        Arrays.fill(maxValue, (byte) 0x01);

        Tlv tlv = new Tlv((short) 0x0424, maxValue);

        assertThat(tlv.length()).isEqualTo(Tlv.MAX_VALUE_LENGTH);
    }

    @Test
    @DisplayName("should throw exception when value exceeds max length")
    void shouldThrowExceptionWhenValueExceedsMaxLength() {
        byte[] tooLarge = new byte[Tlv.MAX_VALUE_LENGTH + 1];

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Tlv((short) 0x0424, tooLarge))
                .withMessageContaining("TLV value length exceeds maximum");
    }

    @Test
    @DisplayName("should handle empty value")
    void shouldHandleEmptyValue() {
        Tlv tlv = new Tlv((short) 0x0001, new byte[0]);

        assertThat(tlv.length()).isEqualTo(0);
        assertThat(tlv.value()).isEmpty();
    }

    @Test
    @DisplayName("should handle unsigned byte values correctly")
    void shouldHandleUnsignedByteValues() {
        Tlv tlv = new Tlv((short) 0x0001, new byte[]{(byte) 0xFF});

        assertThat(tlv.valueAsByte()).isEqualTo(0xFF);
    }

    @Test
    @DisplayName("should handle unsigned short values correctly")
    void shouldHandleUnsignedShortValues() {
        Tlv tlv = new Tlv((short) 0x0001, new byte[]{(byte) 0xFF, (byte) 0xFF});

        assertThat(tlv.valueAsShort()).isEqualTo(0xFFFF);
    }

    @Test
    @DisplayName("should handle unsigned int values correctly")
    void shouldHandleUnsignedIntValues() {
        Tlv tlv = new Tlv((short) 0x0001, new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});

        assertThat(tlv.valueAsInt()).isEqualTo(0xFFFFFFFFL);
    }
}
