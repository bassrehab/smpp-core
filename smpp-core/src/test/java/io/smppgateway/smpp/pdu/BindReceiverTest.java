package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BindReceiver Tests")
class BindReceiverTest {

    @Test
    @DisplayName("should create BindReceiver with builder")
    void shouldCreateBindReceiverWithBuilder() {
        BindReceiver bind = BindReceiver.builder()
                .sequenceNumber(1)
                .systemId("test_esme")
                .password("secret")
                .systemType("MO")
                .interfaceVersion((byte) 0x34)
                .addrTon((byte) 0x01)
                .addrNpi((byte) 0x01)
                .addressRange("*")
                .build();

        assertThat(bind.sequenceNumber()).isEqualTo(1);
        assertThat(bind.systemId()).isEqualTo("test_esme");
        assertThat(bind.password()).isEqualTo("secret");
        assertThat(bind.systemType()).isEqualTo("MO");
        assertThat(bind.interfaceVersion()).isEqualTo((byte) 0x34);
        assertThat(bind.addrTon()).isEqualTo((byte) 0x01);
        assertThat(bind.addrNpi()).isEqualTo((byte) 0x01);
        assertThat(bind.addressRange()).isEqualTo("*");
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        BindReceiver bind = BindReceiver.builder()
                .systemId("test")
                .password("pwd")
                .build();

        assertThat(bind.commandId()).isEqualTo(CommandId.BIND_RECEIVER);
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        BindReceiver bind = BindReceiver.builder()
                .sequenceNumber(123)
                .systemId("my_esme")
                .password("pwd")
                .build();

        BindReceiverResp resp = bind.createResponse();

        assertThat(resp.sequenceNumber()).isEqualTo(123);
        assertThat(resp.commandStatus().isSuccess()).isTrue();
        assertThat(resp.systemId()).isEqualTo("my_esme");
    }

    @Test
    @DisplayName("should create response with custom status")
    void shouldCreateResponseWithCustomStatus() {
        BindReceiver bind = BindReceiver.builder()
                .sequenceNumber(456)
                .systemId("test")
                .password("pwd")
                .build();

        BindReceiverResp resp = bind.createResponse(CommandStatus.ESME_RINVPASWD);

        assertThat(resp.sequenceNumber()).isEqualTo(456);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RINVPASWD);
    }

    @Test
    @DisplayName("should throw exception when systemId is null")
    void shouldThrowExceptionWhenSystemIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> BindReceiver.builder().password("pwd").build())
                .withMessage("systemId required");
    }

    @Test
    @DisplayName("should throw exception when password is null")
    void shouldThrowExceptionWhenPasswordIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> BindReceiver.builder().systemId("test").build())
                .withMessage("password required");
    }

    @Test
    @DisplayName("should use default SMPP version 3.4")
    void shouldUseDefaultSmppVersion34() {
        BindReceiver bind = BindReceiver.builder()
                .systemId("test")
                .password("pwd")
                .build();

        assertThat(bind.interfaceVersion()).isEqualTo(BindTransmitter.SMPP_VERSION_3_4);
    }

    @Test
    @DisplayName("should have immutable optional parameters list")
    void shouldHaveImmutableOptionalParametersList() {
        BindReceiver bind = BindReceiver.builder()
                .systemId("test")
                .password("pwd")
                .build();

        assertThatThrownBy(() -> bind.optionalParameters().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
