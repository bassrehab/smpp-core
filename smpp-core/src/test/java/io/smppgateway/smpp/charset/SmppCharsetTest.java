package io.smppgateway.smpp.charset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppCharset Tests")
class SmppCharsetTest {

    // GSM 7-bit encoding tests

    @Test
    @DisplayName("should encode basic ASCII characters to GSM 7-bit")
    void shouldEncodeBasicAsciiToGsm7() {
        byte[] encoded = SmppCharset.encodeGsm7("Hello");

        assertThat(encoded).hasSize(5);
        // GSM encoding: H=0x48, e=0x65, l=0x6C, l=0x6C, o=0x6F (same as ASCII for these)
    }

    @Test
    @DisplayName("should decode GSM 7-bit to string")
    void shouldDecodeGsm7ToString() {
        byte[] gsm = SmppCharset.encodeGsm7("Hello World");
        String decoded = SmppCharset.decodeGsm7(gsm);

        assertThat(decoded).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("should handle GSM special characters")
    void shouldHandleGsmSpecialCharacters() {
        // Test @ which is 0x00 in GSM
        byte[] encoded = SmppCharset.encodeGsm7("@");
        assertThat(encoded).hasSize(1);
        assertThat(encoded[0]).isEqualTo((byte) 0x00);

        String decoded = SmppCharset.decodeGsm7(encoded);
        assertThat(decoded).isEqualTo("@");
    }

    @Test
    @DisplayName("should handle GSM currency symbols")
    void shouldHandleGsmCurrencySymbols() {
        // £ is 0x01, $ is 0x02, ¥ is 0x03 in GSM
        byte[] encoded = SmppCharset.encodeGsm7("£$¥");
        assertThat(encoded).hasSize(3);

        String decoded = SmppCharset.decodeGsm7(encoded);
        assertThat(decoded).isEqualTo("£$¥");
    }

    @Test
    @DisplayName("should encode extended GSM characters with escape")
    void shouldEncodeExtendedGsmCharactersWithEscape() {
        // Euro sign requires escape sequence in GSM
        byte[] encoded = SmppCharset.encodeGsm7("€");
        assertThat(encoded).hasSize(2); // Escape (0x1B) + 0x65

        String decoded = SmppCharset.decodeGsm7(encoded);
        assertThat(decoded).isEqualTo("€");
    }

    @Test
    @DisplayName("should encode square brackets as extended characters")
    void shouldEncodeSquareBracketsAsExtended() {
        byte[] encoded = SmppCharset.encodeGsm7("[]");
        assertThat(encoded).hasSize(4); // Two escape sequences

        String decoded = SmppCharset.decodeGsm7(encoded);
        assertThat(decoded).isEqualTo("[]");
    }

    @Test
    @DisplayName("should encode curly braces as extended characters")
    void shouldEncodeCurlyBracesAsExtended() {
        byte[] encoded = SmppCharset.encodeGsm7("{}");
        assertThat(encoded).hasSize(4);

        String decoded = SmppCharset.decodeGsm7(encoded);
        assertThat(decoded).isEqualTo("{}");
    }

    @Test
    @DisplayName("should throw exception for non-GSM characters")
    void shouldThrowExceptionForNonGsmCharacters() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SmppCharset.encodeGsm7("中文"))
                .withMessageContaining("cannot be encoded in GSM 7-bit");
    }

    @Test
    @DisplayName("should return empty array for null or empty string")
    void shouldReturnEmptyArrayForNullOrEmptyGsm7() {
        assertThat(SmppCharset.encodeGsm7(null)).isEmpty();
        assertThat(SmppCharset.encodeGsm7("")).isEmpty();
    }

    @Test
    @DisplayName("should return empty string for null or empty GSM bytes")
    void shouldReturnEmptyStringForNullOrEmptyGsmBytes() {
        assertThat(SmppCharset.decodeGsm7(null)).isEmpty();
        assertThat(SmppCharset.decodeGsm7(new byte[0])).isEmpty();
    }

    // UCS-2 encoding tests

    @Test
    @DisplayName("should encode string to UCS-2")
    void shouldEncodeStringToUcs2() {
        byte[] encoded = SmppCharset.encodeUcs2("Hello");

        assertThat(encoded).hasSize(10); // 5 chars * 2 bytes each
    }

    @Test
    @DisplayName("should decode UCS-2 to string")
    void shouldDecodeUcs2ToString() {
        byte[] ucs2 = SmppCharset.encodeUcs2("Hello 世界");
        String decoded = SmppCharset.decodeUcs2(ucs2);

        assertThat(decoded).isEqualTo("Hello 世界");
    }

    @Test
    @DisplayName("should handle Chinese characters in UCS-2")
    void shouldHandleChineseCharactersInUcs2() {
        byte[] encoded = SmppCharset.encodeUcs2("中文测试");
        assertThat(encoded).hasSize(8); // 4 chars * 2 bytes

        String decoded = SmppCharset.decodeUcs2(encoded);
        assertThat(decoded).isEqualTo("中文测试");
    }

    @Test
    @DisplayName("should return empty array for null or empty UCS-2 string")
    void shouldReturnEmptyArrayForNullOrEmptyUcs2() {
        assertThat(SmppCharset.encodeUcs2(null)).isEmpty();
        assertThat(SmppCharset.encodeUcs2("")).isEmpty();
    }

    @Test
    @DisplayName("should return empty string for null or empty UCS-2 bytes")
    void shouldReturnEmptyStringForNullOrEmptyUcs2Bytes() {
        assertThat(SmppCharset.decodeUcs2(null)).isEmpty();
        assertThat(SmppCharset.decodeUcs2(new byte[0])).isEmpty();
    }

    // Latin-1 encoding tests

    @Test
    @DisplayName("should encode string to Latin-1")
    void shouldEncodeStringToLatin1() {
        byte[] encoded = SmppCharset.encodeLatin1("Hello café");

        assertThat(encoded).hasSize(10);
    }

    @Test
    @DisplayName("should decode Latin-1 to string")
    void shouldDecodeLatin1ToString() {
        byte[] latin1 = SmppCharset.encodeLatin1("Tëst mëssägé");
        String decoded = SmppCharset.decodeLatin1(latin1);

        assertThat(decoded).isEqualTo("Tëst mëssägé");
    }

    @Test
    @DisplayName("should return empty array for null or empty Latin-1 string")
    void shouldReturnEmptyArrayForNullOrEmptyLatin1() {
        assertThat(SmppCharset.encodeLatin1(null)).isEmpty();
        assertThat(SmppCharset.encodeLatin1("")).isEmpty();
    }

    @Test
    @DisplayName("should return empty string for null or empty Latin-1 bytes")
    void shouldReturnEmptyStringForNullOrEmptyLatin1Bytes() {
        assertThat(SmppCharset.decodeLatin1(null)).isEmpty();
        assertThat(SmppCharset.decodeLatin1(new byte[0])).isEmpty();
    }

    // canEncodeGsm7 tests

    @Test
    @DisplayName("should return true for GSM-encodable text")
    void shouldReturnTrueForGsmEncodableText() {
        assertThat(SmppCharset.canEncodeGsm7("Hello World!")).isTrue();
        assertThat(SmppCharset.canEncodeGsm7("@£$¥")).isTrue();
        assertThat(SmppCharset.canEncodeGsm7("Test 123")).isTrue();
    }

    @Test
    @DisplayName("should return true for extended GSM characters")
    void shouldReturnTrueForExtendedGsmCharacters() {
        assertThat(SmppCharset.canEncodeGsm7("€")).isTrue();
        assertThat(SmppCharset.canEncodeGsm7("[]{}")).isTrue();
        assertThat(SmppCharset.canEncodeGsm7("~^\\|")).isTrue();
    }

    @Test
    @DisplayName("should return false for non-GSM characters")
    void shouldReturnFalseForNonGsmCharacters() {
        assertThat(SmppCharset.canEncodeGsm7("中文")).isFalse();
        assertThat(SmppCharset.canEncodeGsm7("日本語")).isFalse();
        assertThat(SmppCharset.canEncodeGsm7("العربية")).isFalse();
    }

    @Test
    @DisplayName("should return true for null or empty string")
    void shouldReturnTrueForNullOrEmptyCanEncode() {
        assertThat(SmppCharset.canEncodeGsm7(null)).isTrue();
        assertThat(SmppCharset.canEncodeGsm7("")).isTrue();
    }

    // countGsm7Septets tests

    @Test
    @DisplayName("should count basic characters as 1 septet each")
    void shouldCountBasicCharactersAs1Septet() {
        assertThat(SmppCharset.countGsm7Septets("Hello")).isEqualTo(5);
        assertThat(SmppCharset.countGsm7Septets("Test message")).isEqualTo(12);
    }

    @Test
    @DisplayName("should count extended characters as 2 septets each")
    void shouldCountExtendedCharactersAs2Septets() {
        assertThat(SmppCharset.countGsm7Septets("€")).isEqualTo(2);
        assertThat(SmppCharset.countGsm7Septets("[]")).isEqualTo(4);
        assertThat(SmppCharset.countGsm7Septets("Hi€")).isEqualTo(4); // 2 + 2
    }

    @Test
    @DisplayName("should return -1 for non-GSM text")
    void shouldReturnNegativeOneForNonGsmText() {
        assertThat(SmppCharset.countGsm7Septets("中文")).isEqualTo(-1);
        assertThat(SmppCharset.countGsm7Septets("Test中")).isEqualTo(-1);
    }

    @Test
    @DisplayName("should return 0 for null or empty string")
    void shouldReturnZeroForNullOrEmptySeptets() {
        assertThat(SmppCharset.countGsm7Septets(null)).isZero();
        assertThat(SmppCharset.countGsm7Septets("")).isZero();
    }

    // Round-trip tests

    @Test
    @DisplayName("should round-trip GSM 7-bit encoding")
    void shouldRoundTripGsm7Encoding() {
        String original = "Hello @£$ Test 123!";
        byte[] encoded = SmppCharset.encodeGsm7(original);
        String decoded = SmppCharset.decodeGsm7(encoded);

        assertThat(decoded).isEqualTo(original);
    }

    @Test
    @DisplayName("should round-trip UCS-2 encoding")
    void shouldRoundTripUcs2Encoding() {
        String original = "Hello 世界 🌍";
        byte[] encoded = SmppCharset.encodeUcs2(original);
        String decoded = SmppCharset.decodeUcs2(encoded);

        assertThat(decoded).isEqualTo(original);
    }

    @Test
    @DisplayName("should round-trip Latin-1 encoding")
    void shouldRoundTripLatin1Encoding() {
        String original = "Café résumé naïve";
        byte[] encoded = SmppCharset.encodeLatin1(original);
        String decoded = SmppCharset.decodeLatin1(encoded);

        assertThat(decoded).isEqualTo(original);
    }
}
