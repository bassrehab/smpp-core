package io.smppgateway.smpp.types;

/**
 * SMPP data coding schemes defining how message text is encoded.
 */
public enum DataCoding {
    /**
     * Default SMSC alphabet (usually GSM 7-bit).
     */
    DEFAULT((byte) 0x00),

    /**
     * IA5 (CCITT T.50) / ASCII.
     */
    IA5((byte) 0x01),

    /**
     * Octet unspecified (8-bit binary).
     */
    OCTET_UNSPECIFIED((byte) 0x02),

    /**
     * Latin 1 (ISO-8859-1).
     */
    LATIN1((byte) 0x03),

    /**
     * Octet unspecified (8-bit binary) - common.
     */
    OCTET_UNSPECIFIED_COMMON((byte) 0x04),

    /**
     * JIS (X 0208-1990).
     */
    JIS((byte) 0x05),

    /**
     * Cyrillic (ISO-8859-5).
     */
    CYRILLIC((byte) 0x06),

    /**
     * Latin/Hebrew (ISO-8859-8).
     */
    LATIN_HEBREW((byte) 0x07),

    /**
     * UCS2 (ISO/IEC-10646) - Unicode.
     */
    UCS2((byte) 0x08),

    /**
     * Pictogram encoding.
     */
    PICTOGRAM((byte) 0x09),

    /**
     * ISO-2022-JP (Music Codes).
     */
    ISO_2022_JP((byte) 0x0A),

    /**
     * Extended Kanji JIS (X 0212-1990).
     */
    EXTENDED_KANJI_JIS((byte) 0x0D),

    /**
     * KS C 5601 (Korean).
     */
    KS_C_5601((byte) 0x0E),

    /**
     * GSM 7-bit default alphabet (3GPP TS 23.038).
     */
    GSM7((byte) 0x00),

    /**
     * Flash SMS (Message Class 0 + GSM7).
     */
    FLASH((byte) 0x10),

    /**
     * Flash SMS with UCS2.
     */
    FLASH_UCS2((byte) 0x18);

    private final byte code;

    DataCoding(byte code) {
        this.code = code;
    }

    /**
     * Returns the byte code for this data coding.
     */
    public byte code() {
        return code;
    }

    /**
     * Returns true if this is a Unicode encoding (UCS2).
     */
    public boolean isUnicode() {
        return this == UCS2 || this == FLASH_UCS2;
    }

    /**
     * Returns true if this is GSM 7-bit encoding.
     */
    public boolean isGsm7() {
        return this == DEFAULT || this == GSM7;
    }

    /**
     * Returns true if this is 8-bit binary encoding.
     */
    public boolean is8Bit() {
        return this == OCTET_UNSPECIFIED || this == OCTET_UNSPECIFIED_COMMON || this == LATIN1;
    }

    /**
     * Finds the DataCoding for the given code.
     *
     * @param code the data coding byte
     * @return the DataCoding, or DEFAULT if not found
     */
    public static DataCoding fromCode(byte code) {
        for (DataCoding dc : values()) {
            if (dc.code == code) {
                return dc;
            }
        }
        return DEFAULT;
    }

    /**
     * Alias for fromCode for API compatibility.
     *
     * @param value the data coding byte
     * @return the DataCoding, or DEFAULT if not found
     */
    public static DataCoding fromByte(byte value) {
        return fromCode(value);
    }
}
