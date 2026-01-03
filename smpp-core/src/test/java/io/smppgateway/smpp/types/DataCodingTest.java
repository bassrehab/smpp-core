package io.smppgateway.smpp.types;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DataCoding Tests")
class DataCodingTest {

    @Test
    @DisplayName("should have correct code for default encoding")
    void shouldHaveCorrectCodeForDefault() {
        assertThat(DataCoding.DEFAULT.code()).isEqualTo((byte) 0x00);
    }

    @Test
    @DisplayName("should have correct code for UCS2 encoding")
    void shouldHaveCorrectCodeForUcs2() {
        assertThat(DataCoding.UCS2.code()).isEqualTo((byte) 0x08);
    }

    @Test
    @DisplayName("should have correct code for Latin1 encoding")
    void shouldHaveCorrectCodeForLatin1() {
        assertThat(DataCoding.LATIN1.code()).isEqualTo((byte) 0x03);
    }

    @Test
    @DisplayName("should correctly identify Unicode encodings")
    void shouldIdentifyUnicodeEncodings() {
        assertThat(DataCoding.UCS2.isUnicode()).isTrue();
        assertThat(DataCoding.FLASH_UCS2.isUnicode()).isTrue();

        assertThat(DataCoding.DEFAULT.isUnicode()).isFalse();
        assertThat(DataCoding.LATIN1.isUnicode()).isFalse();
        assertThat(DataCoding.GSM7.isUnicode()).isFalse();
    }

    @Test
    @DisplayName("should correctly identify GSM7 encodings")
    void shouldIdentifyGsm7Encodings() {
        assertThat(DataCoding.DEFAULT.isGsm7()).isTrue();
        assertThat(DataCoding.GSM7.isGsm7()).isTrue();

        assertThat(DataCoding.UCS2.isGsm7()).isFalse();
        assertThat(DataCoding.LATIN1.isGsm7()).isFalse();
    }

    @Test
    @DisplayName("should correctly identify 8-bit encodings")
    void shouldIdentify8BitEncodings() {
        assertThat(DataCoding.OCTET_UNSPECIFIED.is8Bit()).isTrue();
        assertThat(DataCoding.OCTET_UNSPECIFIED_COMMON.is8Bit()).isTrue();
        assertThat(DataCoding.LATIN1.is8Bit()).isTrue();

        assertThat(DataCoding.DEFAULT.is8Bit()).isFalse();
        assertThat(DataCoding.UCS2.is8Bit()).isFalse();
    }

    @Test
    @DisplayName("should find encoding by code")
    void shouldFindEncodingByCode() {
        assertThat(DataCoding.fromCode((byte) 0x00)).isEqualTo(DataCoding.DEFAULT);
        assertThat(DataCoding.fromCode((byte) 0x08)).isEqualTo(DataCoding.UCS2);
        assertThat(DataCoding.fromCode((byte) 0x03)).isEqualTo(DataCoding.LATIN1);
    }

    @Test
    @DisplayName("should return default for unknown code")
    void shouldReturnDefaultForUnknownCode() {
        assertThat(DataCoding.fromCode((byte) 0x99)).isEqualTo(DataCoding.DEFAULT);
        assertThat(DataCoding.fromCode((byte) 0xFF)).isEqualTo(DataCoding.DEFAULT);
    }

    @Test
    @DisplayName("fromByte should work same as fromCode")
    void fromByteShouldWorkSameAsFromCode() {
        assertThat(DataCoding.fromByte((byte) 0x08)).isEqualTo(DataCoding.fromCode((byte) 0x08));
        assertThat(DataCoding.fromByte((byte) 0x03)).isEqualTo(DataCoding.fromCode((byte) 0x03));
    }

    @Test
    @DisplayName("should have correct codes for flash SMS")
    void shouldHaveCorrectCodesForFlashSms() {
        assertThat(DataCoding.FLASH.code()).isEqualTo((byte) 0x10);
        assertThat(DataCoding.FLASH_UCS2.code()).isEqualTo((byte) 0x18);
    }

    @Test
    @DisplayName("should have correct codes for international encodings")
    void shouldHaveCorrectCodesForInternationalEncodings() {
        assertThat(DataCoding.IA5.code()).isEqualTo((byte) 0x01);
        assertThat(DataCoding.CYRILLIC.code()).isEqualTo((byte) 0x06);
        assertThat(DataCoding.LATIN_HEBREW.code()).isEqualTo((byte) 0x07);
        assertThat(DataCoding.JIS.code()).isEqualTo((byte) 0x05);
    }

    @Test
    @DisplayName("should have all expected data coding values")
    void shouldHaveAllExpectedValues() {
        assertThat(DataCoding.values().length).isGreaterThanOrEqualTo(15);
    }
}
