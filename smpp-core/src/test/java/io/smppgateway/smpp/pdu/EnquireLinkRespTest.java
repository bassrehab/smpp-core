package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EnquireLinkResp Tests")
class EnquireLinkRespTest {

    @Test
    @DisplayName("should create response with sequence number and status")
    void shouldCreateResponseWithSequenceNumberAndStatus() {
        EnquireLinkResp resp = new EnquireLinkResp(123, CommandStatus.OK);

        assertThat(resp.sequenceNumber()).isEqualTo(123);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.OK);
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        EnquireLinkResp resp = new EnquireLinkResp(1, CommandStatus.OK);

        assertThat(resp.commandId()).isEqualTo(CommandId.ENQUIRE_LINK_RESP);
    }

    @Test
    @DisplayName("should be a PduResponse")
    void shouldBePduResponse() {
        EnquireLinkResp resp = new EnquireLinkResp(1, CommandStatus.OK);

        assertThat(resp).isInstanceOf(PduResponse.class);
    }

    @Test
    @DisplayName("should support error status")
    void shouldSupportErrorStatus() {
        EnquireLinkResp resp = new EnquireLinkResp(456, CommandStatus.ESME_RSYSERR);

        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RSYSERR);
    }
}
