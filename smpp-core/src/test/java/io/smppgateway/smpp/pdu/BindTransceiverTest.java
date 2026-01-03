package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BindTransceiver Tests")
class BindTransceiverTest {

    @Test
    @DisplayName("should create BindTransceiver with required fields")
    void shouldCreateBindTransceiverWithRequiredFields() {
        BindTransceiver bind = new BindTransceiver(
                1, "test_system", "secret123", null,
                (byte) 0x34, (byte) 0, (byte) 0, null, null
        );

        assertThat(bind.sequenceNumber()).isEqualTo(1);
        assertThat(bind.systemId()).isEqualTo("test_system");
        assertThat(bind.password()).isEqualTo("secret123");
        assertThat(bind.commandId()).isEqualTo(CommandId.BIND_TRANSCEIVER);
    }

    @Test
    @DisplayName("should use default values for null optional fields")
    void shouldUseDefaultValuesForNullFields() {
        BindTransceiver bind = new BindTransceiver(
                1, "system", "password", null,
                (byte) 0x34, (byte) 0, (byte) 0, null, null
        );

        assertThat(bind.systemType()).isEmpty();
        assertThat(bind.addressRange()).isEmpty();
        assertThat(bind.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should throw exception when systemId is null")
    void shouldThrowExceptionWhenSystemIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new BindTransceiver(1, null, "password", null,
                        (byte) 0x34, (byte) 0, (byte) 0, null, null))
                .withMessage("systemId required");
    }

    @Test
    @DisplayName("should throw exception when password is null")
    void shouldThrowExceptionWhenPasswordIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new BindTransceiver(1, "system", null, null,
                        (byte) 0x34, (byte) 0, (byte) 0, null, null))
                .withMessage("password required");
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        BindTransceiver bind = BindTransceiver.builder()
                .sequenceNumber(42)
                .systemId("test_system")
                .password("secret")
                .build();

        BindTransceiverResp response = bind.createResponse();

        assertThat(response.sequenceNumber()).isEqualTo(42);
        assertThat(response.commandStatus()).isEqualTo(CommandStatus.OK);
        assertThat(response.systemId()).isEqualTo("test_system");
    }

    @Test
    @DisplayName("should create response with specified status")
    void shouldCreateResponseWithSpecifiedStatus() {
        BindTransceiver bind = BindTransceiver.builder()
                .sequenceNumber(42)
                .systemId("test_system")
                .password("wrong_password")
                .build();

        BindTransceiverResp response = bind.createResponse(CommandStatus.ESME_RINVPASWD);

        assertThat(response.commandStatus()).isEqualTo(CommandStatus.ESME_RINVPASWD);
    }

    // Builder tests

    @Test
    @DisplayName("builder should create BindTransceiver with all fields")
    void builderShouldCreateBindTransceiverWithAllFields() {
        BindTransceiver bind = BindTransceiver.builder()
                .sequenceNumber(123)
                .systemId("my_esme")
                .password("my_password")
                .systemType("CMT")
                .interfaceVersion((byte) 0x34)
                .addrTon((byte) 0x01)
                .addrNpi((byte) 0x01)
                .addressRange("123")
                .build();

        assertThat(bind.sequenceNumber()).isEqualTo(123);
        assertThat(bind.systemId()).isEqualTo("my_esme");
        assertThat(bind.password()).isEqualTo("my_password");
        assertThat(bind.systemType()).isEqualTo("CMT");
        assertThat(bind.interfaceVersion()).isEqualTo((byte) 0x34);
        assertThat(bind.addrTon()).isEqualTo((byte) 0x01);
        assertThat(bind.addrNpi()).isEqualTo((byte) 0x01);
        assertThat(bind.addressRange()).isEqualTo("123");
    }

    @Test
    @DisplayName("builder should use SMPP 3.4 version by default")
    void builderShouldUseSmpp34VersionByDefault() {
        BindTransceiver bind = BindTransceiver.builder()
                .sequenceNumber(1)
                .systemId("system")
                .password("password")
                .build();

        assertThat(bind.interfaceVersion()).isEqualTo((byte) 0x34);
    }
}
