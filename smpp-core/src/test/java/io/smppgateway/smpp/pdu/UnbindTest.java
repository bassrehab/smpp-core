package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Unbind Tests")
class UnbindTest {

    @Test
    @DisplayName("should create Unbind with sequence number")
    void shouldCreateUnbindWithSequenceNumber() {
        Unbind unbind = new Unbind(54321);

        assertThat(unbind.sequenceNumber()).isEqualTo(54321);
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        Unbind unbind = new Unbind(1);

        assertThat(unbind.commandId()).isEqualTo(CommandId.UNBIND);
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        Unbind unbind = new Unbind(777);

        UnbindResp resp = unbind.createResponse();

        assertThat(resp.sequenceNumber()).isEqualTo(777);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.OK);
    }

    @Test
    @DisplayName("should create response with custom status")
    void shouldCreateResponseWithCustomStatus() {
        Unbind unbind = new Unbind(666);

        UnbindResp resp = unbind.createResponse(CommandStatus.ESME_RSYSERR);

        assertThat(resp.sequenceNumber()).isEqualTo(666);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RSYSERR);
    }

    @Test
    @DisplayName("should have empty optional parameters")
    void shouldHaveEmptyOptionalParameters() {
        Unbind unbind = new Unbind(1);

        assertThat(unbind.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should be a PduRequest")
    void shouldBePduRequest() {
        Unbind unbind = new Unbind(1);

        assertThat(unbind).isInstanceOf(PduRequest.class);
    }
}
