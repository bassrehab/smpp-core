package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.pdu.tlv.TlvTag;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SubmitSmResp Tests")
class SubmitSmRespTest {

    @Test
    @DisplayName("should create response with all fields")
    void shouldCreateResponseWithAllFields() {
        SubmitSmResp resp = new SubmitSmResp(123, CommandStatus.OK, "MSG123456", List.of());

        assertThat(resp.sequenceNumber()).isEqualTo(123);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.OK);
        assertThat(resp.messageId()).isEqualTo("MSG123456");
        assertThat(resp.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        SubmitSmResp resp = new SubmitSmResp(1, CommandStatus.OK, "", List.of());

        assertThat(resp.commandId()).isEqualTo(CommandId.SUBMIT_SM_RESP);
    }

    @Test
    @DisplayName("should report hasMessageId true when message ID present")
    void shouldReportHasMessageIdTrueWhenPresent() {
        SubmitSmResp resp = new SubmitSmResp(1, CommandStatus.OK, "MSG123", List.of());

        assertThat(resp.hasMessageId()).isTrue();
    }

    @Test
    @DisplayName("should report hasMessageId false when message ID is null")
    void shouldReportHasMessageIdFalseWhenNull() {
        SubmitSmResp resp = new SubmitSmResp(1, CommandStatus.ESME_RSUBMITFAIL, null, List.of());

        assertThat(resp.hasMessageId()).isFalse();
    }

    @Test
    @DisplayName("should report hasMessageId false when message ID is empty")
    void shouldReportHasMessageIdFalseWhenEmpty() {
        SubmitSmResp resp = new SubmitSmResp(1, CommandStatus.OK, "", List.of());

        assertThat(resp.hasMessageId()).isFalse();
    }

    @Test
    @DisplayName("should handle null optional parameters")
    void shouldHandleNullOptionalParameters() {
        SubmitSmResp resp = new SubmitSmResp(1, CommandStatus.OK, "MSG", null);

        assertThat(resp.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should have immutable optional parameters")
    void shouldHaveImmutableOptionalParameters() {
        SubmitSmResp resp = new SubmitSmResp(1, CommandStatus.OK, "MSG", List.of());

        assertThatThrownBy(() -> resp.optionalParameters().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should support optional parameters")
    void shouldSupportOptionalParameters() {
        Tlv networkErrorCode = new Tlv(TlvTag.NETWORK_ERROR_CODE, new byte[]{0x03, 0x00, 0x01});
        SubmitSmResp resp = new SubmitSmResp(1, CommandStatus.OK, "MSG", List.of(networkErrorCode));

        assertThat(resp.optionalParameters()).hasSize(1);
    }

    @Test
    @DisplayName("should be a PduResponse")
    void shouldBePduResponse() {
        SubmitSmResp resp = new SubmitSmResp(1, CommandStatus.OK, "", List.of());

        assertThat(resp).isInstanceOf(PduResponse.class);
    }
}
