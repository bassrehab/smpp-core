package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BindTransmitter Tests")
class BindTransmitterTest {

    @Test
    @DisplayName("should create BindTransmitter with builder")
    void shouldCreateBindTransmitterWithBuilder() {
        BindTransmitter bind = BindTransmitter.builder()
                .sequenceNumber(1)
                .systemId("test_esme")
                .password("secret")
                .systemType("CMT")
                .interfaceVersion(BindTransmitter.SMPP_VERSION_3_4)
                .addrTon((byte) 0x01)
                .addrNpi((byte) 0x01)
                .addressRange("123*")
                .build();

        assertThat(bind.sequenceNumber()).isEqualTo(1);
        assertThat(bind.systemId()).isEqualTo("test_esme");
        assertThat(bind.password()).isEqualTo("secret");
        assertThat(bind.systemType()).isEqualTo("CMT");
        assertThat(bind.interfaceVersion()).isEqualTo((byte) 0x34);
        assertThat(bind.addrTon()).isEqualTo((byte) 0x01);
        assertThat(bind.addrNpi()).isEqualTo((byte) 0x01);
        assertThat(bind.addressRange()).isEqualTo("123*");
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        BindTransmitter bind = BindTransmitter.builder()
                .systemId("test")
                .password("pwd")
                .build();

        assertThat(bind.commandId()).isEqualTo(CommandId.BIND_TRANSMITTER);
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        BindTransmitter bind = BindTransmitter.builder()
                .sequenceNumber(123)
                .systemId("test")
                .password("pwd")
                .build();

        BindTransmitterResp resp = bind.createResponse();

        assertThat(resp.sequenceNumber()).isEqualTo(123);
        assertThat(resp.commandStatus().isSuccess()).isTrue();
        assertThat(resp.systemId()).isEqualTo("test");
    }

    @Test
    @DisplayName("should create response with custom status")
    void shouldCreateResponseWithCustomStatus() {
        BindTransmitter bind = BindTransmitter.builder()
                .sequenceNumber(456)
                .systemId("test")
                .password("pwd")
                .build();

        BindTransmitterResp resp = bind.createResponse(CommandStatus.ESME_RBINDFAIL);

        assertThat(resp.sequenceNumber()).isEqualTo(456);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RBINDFAIL);
    }

    @Test
    @DisplayName("should throw exception when systemId is null")
    void shouldThrowExceptionWhenSystemIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> BindTransmitter.builder().password("pwd").build())
                .withMessage("systemId required");
    }

    @Test
    @DisplayName("should throw exception when password is null")
    void shouldThrowExceptionWhenPasswordIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> BindTransmitter.builder().systemId("test").build())
                .withMessage("password required");
    }

    @Test
    @DisplayName("should use default values for optional fields")
    void shouldUseDefaultValuesForOptionalFields() {
        BindTransmitter bind = BindTransmitter.builder()
                .systemId("test")
                .password("pwd")
                .build();

        assertThat(bind.systemType()).isEmpty();
        assertThat(bind.addressRange()).isEmpty();
        assertThat(bind.interfaceVersion()).isEqualTo(BindTransmitter.SMPP_VERSION_3_4);
        assertThat(bind.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should have SMPP version constants")
    void shouldHaveSmppVersionConstants() {
        assertThat(BindTransmitter.SMPP_VERSION_3_3).isEqualTo((byte) 0x33);
        assertThat(BindTransmitter.SMPP_VERSION_3_4).isEqualTo((byte) 0x34);
        assertThat(BindTransmitter.SMPP_VERSION_5_0).isEqualTo((byte) 0x50);
    }

    @Test
    @DisplayName("should have immutable optional parameters list")
    void shouldHaveImmutableOptionalParametersList() {
        BindTransmitter bind = BindTransmitter.builder()
                .systemId("test")
                .password("pwd")
                .build();

        assertThatThrownBy(() -> bind.optionalParameters().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
