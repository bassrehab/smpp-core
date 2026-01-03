package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Outbind Tests")
class OutbindTest {

    @Test
    @DisplayName("should create Outbind with all fields")
    void shouldCreateOutbindWithAllFields() {
        Outbind outbind = new Outbind(1, "SMSC", "password", List.of());

        assertThat(outbind.sequenceNumber()).isEqualTo(1);
        assertThat(outbind.systemId()).isEqualTo("SMSC");
        assertThat(outbind.password()).isEqualTo("password");
        assertThat(outbind.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        Outbind outbind = new Outbind(1, "", "", List.of());

        assertThat(outbind.commandId()).isEqualTo(CommandId.OUTBIND);
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        Outbind outbind = new Outbind(123, "SMSC", "", List.of());

        BindTransceiverResp resp = outbind.createResponse();

        assertThat(resp.sequenceNumber()).isEqualTo(123);
        assertThat(resp.commandStatus().isSuccess()).isTrue();
    }

    @Test
    @DisplayName("should create response with custom status")
    void shouldCreateResponseWithCustomStatus() {
        Outbind outbind = new Outbind(456, "SMSC", "", List.of());

        BindTransceiverResp resp = outbind.createResponse(CommandStatus.ESME_RBINDFAIL);

        assertThat(resp.sequenceNumber()).isEqualTo(456);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RBINDFAIL);
    }

    @Test
    @DisplayName("should handle null systemId as empty string")
    void shouldHandleNullSystemIdAsEmptyString() {
        Outbind outbind = new Outbind(1, null, "", List.of());

        assertThat(outbind.systemId()).isEmpty();
    }

    @Test
    @DisplayName("should handle null password as empty string")
    void shouldHandleNullPasswordAsEmptyString() {
        Outbind outbind = new Outbind(1, "", null, List.of());

        assertThat(outbind.password()).isEmpty();
    }

    @Test
    @DisplayName("should have immutable optional parameters")
    void shouldHaveImmutableOptionalParameters() {
        Outbind outbind = new Outbind(1, "", "", List.of());

        assertThatThrownBy(() -> outbind.optionalParameters().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should be a PduRequest")
    void shouldBePduRequest() {
        Outbind outbind = new Outbind(1, "", "", List.of());

        assertThat(outbind).isInstanceOf(PduRequest.class);
    }
}
