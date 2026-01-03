package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UnbindResp Tests")
class UnbindRespTest {

    @Test
    @DisplayName("should create response with sequence number and status")
    void shouldCreateResponseWithSequenceNumberAndStatus() {
        UnbindResp resp = new UnbindResp(789, CommandStatus.OK);

        assertThat(resp.sequenceNumber()).isEqualTo(789);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.OK);
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        UnbindResp resp = new UnbindResp(1, CommandStatus.OK);

        assertThat(resp.commandId()).isEqualTo(CommandId.UNBIND_RESP);
    }

    @Test
    @DisplayName("should be a PduResponse")
    void shouldBePduResponse() {
        UnbindResp resp = new UnbindResp(1, CommandStatus.OK);

        assertThat(resp).isInstanceOf(PduResponse.class);
    }

    @Test
    @DisplayName("should support error status")
    void shouldSupportErrorStatus() {
        UnbindResp resp = new UnbindResp(999, CommandStatus.ESME_RSYSERR);

        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RSYSERR);
    }
}
