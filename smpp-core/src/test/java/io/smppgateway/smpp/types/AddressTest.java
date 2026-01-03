package io.smppgateway.smpp.types;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Address Tests")
class AddressTest {

    @Test
    @DisplayName("should create address with TON, NPI, and address string")
    void shouldCreateAddressWithTonNpiAndAddress() {
        Address address = new Address(Address.TON_INTERNATIONAL, Address.NPI_E164, "+14155551234");

        assertThat(address.ton()).isEqualTo(Address.TON_INTERNATIONAL);
        assertThat(address.npi()).isEqualTo(Address.NPI_E164);
        assertThat(address.address()).isEqualTo("+14155551234");
    }

    @Test
    @DisplayName("should create address with just address string using defaults")
    void shouldCreateAddressWithDefaultTonNpi() {
        Address address = new Address("12345");

        assertThat(address.ton()).isEqualTo(Address.TON_UNKNOWN);
        assertThat(address.npi()).isEqualTo(Address.NPI_UNKNOWN);
        assertThat(address.address()).isEqualTo("12345");
    }

    @Test
    @DisplayName("should create international address using factory method")
    void shouldCreateInternationalAddress() {
        Address address = Address.international("+14155551234");

        assertThat(address.ton()).isEqualTo(Address.TON_INTERNATIONAL);
        assertThat(address.npi()).isEqualTo(Address.NPI_E164);
        assertThat(address.address()).isEqualTo("+14155551234");
    }

    @Test
    @DisplayName("should create alphanumeric address using factory method")
    void shouldCreateAlphanumericAddress() {
        Address address = Address.alphanumeric("MYCOMPANY");

        assertThat(address.ton()).isEqualTo(Address.TON_ALPHANUMERIC);
        assertThat(address.npi()).isEqualTo(Address.NPI_UNKNOWN);
        assertThat(address.address()).isEqualTo("MYCOMPANY");
    }

    @Test
    @DisplayName("should create national address using factory method")
    void shouldCreateNationalAddress() {
        Address address = Address.national("5551234");

        assertThat(address.ton()).isEqualTo(Address.TON_NATIONAL);
        assertThat(address.npi()).isEqualTo(Address.NPI_E164);
        assertThat(address.address()).isEqualTo("5551234");
    }

    @Test
    @DisplayName("should throw exception when address is null")
    void shouldThrowExceptionWhenAddressIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Address(Address.TON_UNKNOWN, Address.NPI_UNKNOWN, null))
                .withMessage("address must not be null");
    }

    @Test
    @DisplayName("should throw exception when address exceeds max length")
    void shouldThrowExceptionWhenAddressExceedsMaxLength() {
        String longAddress = "A".repeat(Address.MAX_ADDRESS_LENGTH + 1);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Address(longAddress))
                .withMessageContaining("Address length exceeds maximum");
    }

    @Test
    @DisplayName("should accept address at max length")
    void shouldAcceptAddressAtMaxLength() {
        String maxLengthAddress = "A".repeat(Address.MAX_ADDRESS_LENGTH);

        Address address = new Address(maxLengthAddress);

        assertThat(address.address()).hasSize(Address.MAX_ADDRESS_LENGTH);
    }

    @Test
    @DisplayName("should convert to bytes with null terminator")
    void shouldConvertToBytesWithNullTerminator() {
        Address address = new Address("12345");

        byte[] bytes = address.toBytes();

        assertThat(bytes).hasSize(6); // 5 chars + null terminator
        assertThat(bytes[0]).isEqualTo((byte) '1');
        assertThat(bytes[1]).isEqualTo((byte) '2');
        assertThat(bytes[2]).isEqualTo((byte) '3');
        assertThat(bytes[3]).isEqualTo((byte) '4');
        assertThat(bytes[4]).isEqualTo((byte) '5');
        assertThat(bytes[5]).isEqualTo((byte) 0); // null terminator
    }

    @Test
    @DisplayName("should convert empty address to bytes with just null terminator")
    void shouldConvertEmptyAddressToBytes() {
        Address address = new Address("");

        byte[] bytes = address.toBytes();

        assertThat(bytes).hasSize(1);
        assertThat(bytes[0]).isEqualTo((byte) 0);
    }

    @Test
    @DisplayName("should have correct TON constants")
    void shouldHaveCorrectTonConstants() {
        assertThat(Address.TON_UNKNOWN).isEqualTo((byte) 0x00);
        assertThat(Address.TON_INTERNATIONAL).isEqualTo((byte) 0x01);
        assertThat(Address.TON_NATIONAL).isEqualTo((byte) 0x02);
        assertThat(Address.TON_NETWORK_SPECIFIC).isEqualTo((byte) 0x03);
        assertThat(Address.TON_SUBSCRIBER_NUMBER).isEqualTo((byte) 0x04);
        assertThat(Address.TON_ALPHANUMERIC).isEqualTo((byte) 0x05);
        assertThat(Address.TON_ABBREVIATED).isEqualTo((byte) 0x06);
    }

    @Test
    @DisplayName("should have correct NPI constants")
    void shouldHaveCorrectNpiConstants() {
        assertThat(Address.NPI_UNKNOWN).isEqualTo((byte) 0x00);
        assertThat(Address.NPI_E164).isEqualTo((byte) 0x01);
        assertThat(Address.NPI_DATA).isEqualTo((byte) 0x03);
        assertThat(Address.NPI_TELEX).isEqualTo((byte) 0x04);
        assertThat(Address.NPI_LAND_MOBILE).isEqualTo((byte) 0x06);
        assertThat(Address.NPI_NATIONAL).isEqualTo((byte) 0x08);
        assertThat(Address.NPI_PRIVATE).isEqualTo((byte) 0x09);
        assertThat(Address.NPI_ERMES).isEqualTo((byte) 0x0A);
        assertThat(Address.NPI_INTERNET).isEqualTo((byte) 0x0E);
        assertThat(Address.NPI_WAP_CLIENT_ID).isEqualTo((byte) 0x12);
    }

    @Test
    @DisplayName("should have meaningful toString output")
    void shouldHaveMeaningfulToString() {
        Address address = new Address(Address.TON_INTERNATIONAL, Address.NPI_E164, "+14155551234");

        String result = address.toString();

        assertThat(result).contains("1"); // TON
        assertThat(result).contains("+14155551234");
    }

    @Test
    @DisplayName("should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        Address address1 = new Address(Address.TON_INTERNATIONAL, Address.NPI_E164, "+14155551234");
        Address address2 = new Address(Address.TON_INTERNATIONAL, Address.NPI_E164, "+14155551234");
        Address address3 = new Address(Address.TON_NATIONAL, Address.NPI_E164, "+14155551234");

        assertThat(address1).isEqualTo(address2);
        assertThat(address1).isNotEqualTo(address3);
    }

    @Test
    @DisplayName("should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        Address address1 = new Address(Address.TON_INTERNATIONAL, Address.NPI_E164, "+14155551234");
        Address address2 = new Address(Address.TON_INTERNATIONAL, Address.NPI_E164, "+14155551234");

        assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1", "12345678901234567890", "MYCOMPANY", "+1-555-123-4567"})
    @DisplayName("should accept various valid address formats")
    void shouldAcceptVariousAddressFormats(String addressStr) {
        Address address = new Address(addressStr);

        assertThat(address.address()).isEqualTo(addressStr);
    }
}
