package io.smppgateway.smpp.pdu.tlv;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a TLV (Tag-Length-Value) optional parameter in SMPP PDUs.
 *
 * @param tag   the TLV tag code (2 bytes)
 * @param value the TLV value bytes
 */
public record Tlv(short tag, byte[] value) {

    /**
     * Maximum TLV value length.
     */
    public static final int MAX_VALUE_LENGTH = 65535;

    /**
     * Compact constructor with validation and defensive copy.
     */
    public Tlv {
        Objects.requireNonNull(value, "value must not be null");
        if (value.length > MAX_VALUE_LENGTH) {
            throw new IllegalArgumentException(
                    "TLV value length exceeds maximum: " + value.length);
        }
        value = value.clone(); // defensive copy
    }

    /**
     * Creates a TLV from a standard tag.
     *
     * @param tag   the standard TLV tag
     * @param value the value bytes
     */
    public Tlv(TlvTag tag, byte[] value) {
        this(tag.code(), value);
    }

    /**
     * Returns the value length.
     */
    public int length() {
        return value.length;
    }

    /**
     * Returns the raw tag value. Alias for tag().
     */
    public short rawTag() {
        return tag;
    }

    /**
     * Returns a copy of the value bytes.
     */
    @Override
    public byte[] value() {
        return value.clone();
    }

    /**
     * Returns the value as a string (null-terminated C-string).
     */
    public String valueAsString() {
        int len = value.length;
        // Remove null terminator if present
        if (len > 0 && value[len - 1] == 0) {
            len--;
        }
        return new String(value, 0, len, StandardCharsets.ISO_8859_1);
    }

    /**
     * Returns the value as an unsigned byte (1-byte value).
     */
    public int valueAsByte() {
        if (value.length < 1) {
            throw new IllegalStateException("TLV value is empty");
        }
        return value[0] & 0xFF;
    }

    /**
     * Returns the value as an unsigned short (2-byte value, big-endian).
     */
    public int valueAsShort() {
        if (value.length < 2) {
            throw new IllegalStateException("TLV value too short for short");
        }
        return ((value[0] & 0xFF) << 8) | (value[1] & 0xFF);
    }

    /**
     * Returns the value as an unsigned int (4-byte value, big-endian).
     */
    public long valueAsInt() {
        if (value.length < 4) {
            throw new IllegalStateException("TLV value too short for int");
        }
        return ((long) (value[0] & 0xFF) << 24) |
               ((long) (value[1] & 0xFF) << 16) |
               ((long) (value[2] & 0xFF) << 8) |
               (value[3] & 0xFF);
    }

    /**
     * Returns the standard TLV tag if known, null otherwise.
     */
    public TlvTag knownTag() {
        return TlvTag.fromCode(tag);
    }

    /**
     * Creates a TLV with a string value (null-terminated).
     *
     * @param tag   the TLV tag
     * @param value the string value
     * @return the TLV
     */
    public static Tlv ofString(TlvTag tag, String value) {
        byte[] strBytes = value.getBytes(StandardCharsets.ISO_8859_1);
        byte[] withNull = Arrays.copyOf(strBytes, strBytes.length + 1);
        return new Tlv(tag, withNull);
    }

    /**
     * Creates a TLV with a single byte value.
     *
     * @param tag   the TLV tag
     * @param value the byte value
     * @return the TLV
     */
    public static Tlv ofByte(TlvTag tag, byte value) {
        return new Tlv(tag, new byte[]{value});
    }

    /**
     * Creates a TLV with a short value (2 bytes, big-endian).
     *
     * @param tag   the TLV tag
     * @param value the short value
     * @return the TLV
     */
    public static Tlv ofShort(TlvTag tag, short value) {
        return new Tlv(tag, new byte[]{
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        });
    }

    /**
     * Creates a TLV with an int value (4 bytes, big-endian).
     *
     * @param tag   the TLV tag
     * @param value the int value
     * @return the TLV
     */
    public static Tlv ofInt(TlvTag tag, int value) {
        return new Tlv(tag, new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tlv tlv)) return false;
        return tag == tlv.tag && Arrays.equals(value, tlv.value);
    }

    @Override
    public int hashCode() {
        int result = Short.hashCode(tag);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public String toString() {
        TlvTag knownTag = knownTag();
        String tagStr = knownTag != null ? knownTag.name() : String.format("0x%04X", tag);
        return String.format("Tlv[%s, length=%d]", tagStr, value.length);
    }
}
