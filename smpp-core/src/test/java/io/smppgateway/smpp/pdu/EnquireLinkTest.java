package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EnquireLink Tests")
class EnquireLinkTest {

    @Test
    @DisplayName("should create EnquireLink with sequence number")
    void shouldCreateEnquireLinkWithSequenceNumber() {
        EnquireLink enquire = new EnquireLink(12345);

        assertThat(enquire.sequenceNumber()).isEqualTo(12345);
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        EnquireLink enquire = new EnquireLink(1);

        assertThat(enquire.commandId()).isEqualTo(CommandId.ENQUIRE_LINK);
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        EnquireLink enquire = new EnquireLink(999);

        EnquireLinkResp resp = enquire.createResponse();

        assertThat(resp.sequenceNumber()).isEqualTo(999);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.OK);
    }

    @Test
    @DisplayName("should create response with custom status")
    void shouldCreateResponseWithCustomStatus() {
        EnquireLink enquire = new EnquireLink(888);

        EnquireLinkResp resp = enquire.createResponse(CommandStatus.ESME_RSYSERR);

        assertThat(resp.sequenceNumber()).isEqualTo(888);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RSYSERR);
    }

    @Test
    @DisplayName("should have empty optional parameters")
    void shouldHaveEmptyOptionalParameters() {
        EnquireLink enquire = new EnquireLink(1);

        assertThat(enquire.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should be a PduRequest")
    void shouldBePduRequest() {
        EnquireLink enquire = new EnquireLink(1);

        assertThat(enquire).isInstanceOf(PduRequest.class);
    }
}
