package io.smppgateway.smpp.types;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CommandId Tests")
class CommandIdTest {

    @Test
    @DisplayName("should have correct codes for bind operations")
    void shouldHaveCorrectCodesForBindOperations() {
        assertThat(CommandId.BIND_RECEIVER.code()).isEqualTo(0x00000001);
        assertThat(CommandId.BIND_RECEIVER_RESP.code()).isEqualTo(0x80000001);
        assertThat(CommandId.BIND_TRANSMITTER.code()).isEqualTo(0x00000002);
        assertThat(CommandId.BIND_TRANSMITTER_RESP.code()).isEqualTo(0x80000002);
        assertThat(CommandId.BIND_TRANSCEIVER.code()).isEqualTo(0x00000009);
        assertThat(CommandId.BIND_TRANSCEIVER_RESP.code()).isEqualTo(0x80000009);
    }

    @Test
    @DisplayName("should have correct codes for message operations")
    void shouldHaveCorrectCodesForMessageOperations() {
        assertThat(CommandId.SUBMIT_SM.code()).isEqualTo(0x00000004);
        assertThat(CommandId.SUBMIT_SM_RESP.code()).isEqualTo(0x80000004);
        assertThat(CommandId.DELIVER_SM.code()).isEqualTo(0x00000005);
        assertThat(CommandId.DELIVER_SM_RESP.code()).isEqualTo(0x80000005);
        assertThat(CommandId.DATA_SM.code()).isEqualTo(0x00000103);
        assertThat(CommandId.DATA_SM_RESP.code()).isEqualTo(0x80000103);
    }

    @Test
    @DisplayName("should have correct codes for session management")
    void shouldHaveCorrectCodesForSessionManagement() {
        assertThat(CommandId.UNBIND.code()).isEqualTo(0x00000006);
        assertThat(CommandId.UNBIND_RESP.code()).isEqualTo(0x80000006);
        assertThat(CommandId.ENQUIRE_LINK.code()).isEqualTo(0x00000015);
        assertThat(CommandId.ENQUIRE_LINK_RESP.code()).isEqualTo(0x80000015);
    }

    @Test
    @DisplayName("should have correct code for generic nack")
    void shouldHaveCorrectCodeForGenericNack() {
        assertThat(CommandId.GENERIC_NACK.code()).isEqualTo(0x80000000);
    }

    @Test
    @DisplayName("should correctly identify response commands")
    void shouldIdentifyResponseCommands() {
        assertThat(CommandId.BIND_RECEIVER_RESP.isResponse()).isTrue();
        assertThat(CommandId.SUBMIT_SM_RESP.isResponse()).isTrue();
        assertThat(CommandId.GENERIC_NACK.isResponse()).isTrue();

        assertThat(CommandId.BIND_RECEIVER.isResponse()).isFalse();
        assertThat(CommandId.SUBMIT_SM.isResponse()).isFalse();
    }

    @Test
    @DisplayName("should correctly identify request commands")
    void shouldIdentifyRequestCommands() {
        assertThat(CommandId.BIND_RECEIVER.isRequest()).isTrue();
        assertThat(CommandId.SUBMIT_SM.isRequest()).isTrue();
        assertThat(CommandId.ENQUIRE_LINK.isRequest()).isTrue();

        assertThat(CommandId.BIND_RECEIVER_RESP.isRequest()).isFalse();
        assertThat(CommandId.SUBMIT_SM_RESP.isRequest()).isFalse();
        assertThat(CommandId.GENERIC_NACK.isRequest()).isFalse();
    }

    @Test
    @DisplayName("should find command by code")
    void shouldFindCommandByCode() {
        assertThat(CommandId.fromCode(0x00000004)).isEqualTo(CommandId.SUBMIT_SM);
        assertThat(CommandId.fromCode(0x80000004)).isEqualTo(CommandId.SUBMIT_SM_RESP);
        assertThat(CommandId.fromCode(0x00000015)).isEqualTo(CommandId.ENQUIRE_LINK);
    }

    @Test
    @DisplayName("should return null for unknown command code")
    void shouldReturnNullForUnknownCode() {
        assertThat(CommandId.fromCode(0x99999999)).isNull();
        assertThat(CommandId.fromCode(0x00000000)).isNull();
    }

    @Test
    @DisplayName("should convert request to response command")
    void shouldConvertRequestToResponse() {
        assertThat(CommandId.BIND_RECEIVER.toResponse()).isEqualTo(CommandId.BIND_RECEIVER_RESP);
        assertThat(CommandId.SUBMIT_SM.toResponse()).isEqualTo(CommandId.SUBMIT_SM_RESP);
        assertThat(CommandId.ENQUIRE_LINK.toResponse()).isEqualTo(CommandId.ENQUIRE_LINK_RESP);
    }

    @Test
    @DisplayName("should throw exception when converting response to response")
    void shouldThrowExceptionWhenConvertingResponseToResponse() {
        assertThatIllegalStateException()
                .isThrownBy(() -> CommandId.SUBMIT_SM_RESP.toResponse())
                .withMessageContaining("Already a response");
    }

    @ParameterizedTest
    @EnumSource(value = CommandId.class, names = {".*_RESP", "GENERIC_NACK"}, mode = EnumSource.Mode.MATCH_ANY)
    @DisplayName("all response commands should have high bit set")
    void allResponsesShouldHaveHighBitSet(CommandId commandId) {
        assertThat(commandId.code() & 0x80000000).isNotZero();
    }

    @ParameterizedTest
    @EnumSource(value = CommandId.class, names = {".*_RESP", "GENERIC_NACK"}, mode = EnumSource.Mode.MATCH_NONE)
    @DisplayName("all request commands should not have high bit set")
    void allRequestsShouldNotHaveHighBitSet(CommandId commandId) {
        assertThat(commandId.code() & 0x80000000).isZero();
    }

    @Test
    @DisplayName("should have all expected command types")
    void shouldHaveAllExpectedCommandTypes() {
        assertThat(CommandId.values()).hasSize(27);
    }
}
