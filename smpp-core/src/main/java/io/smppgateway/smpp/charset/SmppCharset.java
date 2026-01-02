package io.smppgateway.smpp.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * SMPP charset utilities for encoding and decoding SMS messages.
 *
 * <p>Supports:
 * <ul>
 *   <li>GSM 7-bit default alphabet (GSM 03.38)</li>
 *   <li>UCS-2 (UTF-16BE) for Unicode messages</li>
 *   <li>ISO-8859-1 (Latin-1)</li>
 * </ul>
 */
public final class SmppCharset {

    private SmppCharset() {}

    /**
     * GSM 7-bit default alphabet mapping table.
     * Maps GSM character codes (0x00-0x7F) to Unicode characters.
     */
    private static final char[] GSM_TO_UNICODE = {
        '@', '£', '$', '¥', 'è', 'é', 'ù', 'ì', 'ò', 'Ç', '\n', 'Ø', 'ø', '\r', 'Å', 'å',
        'Δ', '_', 'Φ', 'Γ', 'Λ', 'Ω', 'Π', 'Ψ', 'Σ', 'Θ', 'Ξ', '\u001B', 'Æ', 'æ', 'ß', 'É',
        ' ', '!', '"', '#', '¤', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
        '¡', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ñ', 'Ü', '§',
        '¿', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ñ', 'ü', 'à'
    };

    /**
     * GSM extended table characters (accessed via escape 0x1B).
     */
    private static final char[] GSM_EXTENDED = new char[128];
    static {
        // Most extended chars map to space (not supported)
        for (int i = 0; i < 128; i++) {
            GSM_EXTENDED[i] = ' ';
        }
        // Specific extended characters
        GSM_EXTENDED[0x0A] = '\f';  // Form feed
        GSM_EXTENDED[0x14] = '^';
        GSM_EXTENDED[0x28] = '{';
        GSM_EXTENDED[0x29] = '}';
        GSM_EXTENDED[0x2F] = '\\';
        GSM_EXTENDED[0x3C] = '[';
        GSM_EXTENDED[0x3D] = '~';
        GSM_EXTENDED[0x3E] = ']';
        GSM_EXTENDED[0x40] = '|';
        GSM_EXTENDED[0x65] = '€';
    }

    /**
     * Reverse mapping from Unicode to GSM.
     */
    private static final int[] UNICODE_TO_GSM = new int[65536];
    static {
        // Initialize all to -1 (not mappable)
        for (int i = 0; i < UNICODE_TO_GSM.length; i++) {
            UNICODE_TO_GSM[i] = -1;
        }
        // Map basic characters
        for (int i = 0; i < GSM_TO_UNICODE.length; i++) {
            UNICODE_TO_GSM[GSM_TO_UNICODE[i]] = i;
        }
        // Map extended characters (with escape prefix marker)
        UNICODE_TO_GSM['\f'] = 0x1B0A;
        UNICODE_TO_GSM['^'] = 0x1B14;
        UNICODE_TO_GSM['{'] = 0x1B28;
        UNICODE_TO_GSM['}'] = 0x1B29;
        UNICODE_TO_GSM['\\'] = 0x1B2F;
        UNICODE_TO_GSM['['] = 0x1B3C;
        UNICODE_TO_GSM['~'] = 0x1B3D;
        UNICODE_TO_GSM[']'] = 0x1B3E;
        UNICODE_TO_GSM['|'] = 0x1B40;
        UNICODE_TO_GSM['€'] = 0x1B65;
    }

    /**
     * Encodes a string to GSM 7-bit default alphabet.
     *
     * @param text The text to encode
     * @return GSM encoded bytes
     * @throws IllegalArgumentException if text contains unencodable characters
     */
    public static byte[] encodeGsm7(String text) {
        if (text == null || text.isEmpty()) {
            return new byte[0];
        }

        // First pass: calculate required size
        int size = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int gsm = c < UNICODE_TO_GSM.length ? UNICODE_TO_GSM[c] : -1;
            if (gsm < 0) {
                throw new IllegalArgumentException(
                    "Character '" + c + "' (U+" + String.format("%04X", (int) c) +
                    ") cannot be encoded in GSM 7-bit");
            }
            size += (gsm > 0xFF) ? 2 : 1;  // Extended chars need escape byte
        }

        // Second pass: encode
        byte[] result = new byte[size];
        int pos = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int gsm = UNICODE_TO_GSM[c];
            if (gsm > 0xFF) {
                // Extended character: write escape + char
                result[pos++] = 0x1B;
                result[pos++] = (byte) (gsm & 0xFF);
            } else {
                result[pos++] = (byte) gsm;
            }
        }
        return result;
    }

    /**
     * Decodes GSM 7-bit bytes to a string.
     *
     * @param data GSM encoded bytes
     * @return Decoded string
     */
    public static String decodeGsm7(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(data.length);
        boolean escape = false;

        for (byte b : data) {
            int code = b & 0x7F;
            if (escape) {
                sb.append(GSM_EXTENDED[code]);
                escape = false;
            } else if (code == 0x1B) {
                escape = true;
            } else {
                sb.append(GSM_TO_UNICODE[code]);
            }
        }
        return sb.toString();
    }

    /**
     * Encodes a string to UCS-2 (UTF-16BE).
     *
     * @param text The text to encode
     * @return UCS-2 encoded bytes
     */
    public static byte[] encodeUcs2(String text) {
        if (text == null || text.isEmpty()) {
            return new byte[0];
        }
        return text.getBytes(StandardCharsets.UTF_16BE);
    }

    /**
     * Decodes UCS-2 (UTF-16BE) bytes to a string.
     *
     * @param data UCS-2 encoded bytes
     * @return Decoded string
     */
    public static String decodeUcs2(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        return new String(data, StandardCharsets.UTF_16BE);
    }

    /**
     * Encodes a string to ISO-8859-1 (Latin-1).
     *
     * @param text The text to encode
     * @return Latin-1 encoded bytes
     */
    public static byte[] encodeLatin1(String text) {
        if (text == null || text.isEmpty()) {
            return new byte[0];
        }
        return text.getBytes(StandardCharsets.ISO_8859_1);
    }

    /**
     * Decodes ISO-8859-1 (Latin-1) bytes to a string.
     *
     * @param data Latin-1 encoded bytes
     * @return Decoded string
     */
    public static String decodeLatin1(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        return new String(data, StandardCharsets.ISO_8859_1);
    }

    /**
     * Checks if a string can be encoded using GSM 7-bit alphabet.
     *
     * @param text The text to check
     * @return true if all characters are GSM-encodable
     */
    public static boolean canEncodeGsm7(String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= UNICODE_TO_GSM.length || UNICODE_TO_GSM[c] < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the number of GSM 7-bit characters (septets) required.
     * Extended characters count as 2 septets.
     *
     * @param text The text to measure
     * @return Number of septets, or -1 if not GSM-encodable
     */
    public static int countGsm7Septets(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= UNICODE_TO_GSM.length) {
                return -1;
            }
            int gsm = UNICODE_TO_GSM[c];
            if (gsm < 0) {
                return -1;
            }
            count += (gsm > 0xFF) ? 2 : 1;
        }
        return count;
    }
}
