package io.smppgateway.smpp.types;

import java.util.Objects;

/**
 * Represents an SMPP address consisting of TON (Type of Number),
 * NPI (Numbering Plan Indicator), and the address string.
 *
 * @param ton     Type of Number (0-6)
 * @param npi     Numbering Plan Indicator (0-18)
 * @param address The address string (max 21 characters)
 */
public record Address(byte ton, byte npi, String address) {

    /**
     * Common TON values.
     */
    public static final byte TON_UNKNOWN = 0x00;
    public static final byte TON_INTERNATIONAL = 0x01;
    public static final byte TON_NATIONAL = 0x02;
    public static final byte TON_NETWORK_SPECIFIC = 0x03;
    public static final byte TON_SUBSCRIBER_NUMBER = 0x04;
    public static final byte TON_ALPHANUMERIC = 0x05;
    public static final byte TON_ABBREVIATED = 0x06;

    /**
     * Common NPI values.
     */
    public static final byte NPI_UNKNOWN = 0x00;
    public static final byte NPI_E164 = 0x01;
    public static final byte NPI_DATA = 0x03;
    public static final byte NPI_TELEX = 0x04;
    public static final byte NPI_LAND_MOBILE = 0x06;
    public static final byte NPI_NATIONAL = 0x08;
    public static final byte NPI_PRIVATE = 0x09;
    public static final byte NPI_ERMES = 0x0A;
    public static final byte NPI_INTERNET = 0x0E;
    public static final byte NPI_WAP_CLIENT_ID = 0x12;

    /**
     * Maximum address length.
     */
    public static final int MAX_ADDRESS_LENGTH = 21;

    /**
     * Compact constructor with validation.
     */
    public Address {
        Objects.requireNonNull(address, "address must not be null");
        if (address.length() > MAX_ADDRESS_LENGTH) {
            throw new IllegalArgumentException(
                    "Address length exceeds maximum of " + MAX_ADDRESS_LENGTH + ": " + address.length());
        }
    }

    /**
     * Creates an address with default TON and NPI (unknown).
     *
     * @param address the address string
     */
    public Address(String address) {
        this(TON_UNKNOWN, NPI_UNKNOWN, address);
    }

    /**
     * Creates an international E.164 address.
     *
     * @param address the address string (e.g., "+14155551234")
     * @return the Address
     */
    public static Address international(String address) {
        return new Address(TON_INTERNATIONAL, NPI_E164, address);
    }

    /**
     * Creates an alphanumeric address (sender ID).
     *
     * @param address the alphanumeric address (e.g., "MYCOMPANY")
     * @return the Address
     */
    public static Address alphanumeric(String address) {
        return new Address(TON_ALPHANUMERIC, NPI_UNKNOWN, address);
    }

    /**
     * Creates a national address.
     *
     * @param address the national address string
     * @return the Address
     */
    public static Address national(String address) {
        return new Address(TON_NATIONAL, NPI_E164, address);
    }

    /**
     * Returns the address as a C-Octet string (null-terminated bytes).
     *
     * @return byte array with null terminator
     */
    public byte[] toBytes() {
        byte[] bytes = new byte[address.length() + 1];
        System.arraycopy(address.getBytes(), 0, bytes, 0, address.length());
        bytes[address.length()] = 0; // null terminator
        return bytes;
    }

    @Override
    public String toString() {
        return String.format("Address[ton=%d, npi=%d, address=%s]", ton, npi, address);
    }
}
