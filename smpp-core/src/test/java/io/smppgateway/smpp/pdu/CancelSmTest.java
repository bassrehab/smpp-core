package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CancelSm Tests")
class CancelSmTest {

    @Test
    @DisplayName("should create CancelSm with all fields")
    void shouldCreateCancelSmWithAllFields() {
        Address source = Address.international("+14155551234");
        Address dest = Address.international("+14155555678");

        CancelSm cancel = new CancelSm(1, "CMT", "MSG123", source, dest, List.of());

        assertThat(cancel.sequenceNumber()).isEqualTo(1);
        assertThat(cancel.serviceType()).isEqualTo("CMT");
        assertThat(cancel.messageId()).isEqualTo("MSG123");
        assertThat(cancel.sourceAddress()).isEqualTo(source);
        assertThat(cancel.destAddress()).isEqualTo(dest);
        assertThat(cancel.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        CancelSm cancel = new CancelSm(1, "", "", null, null, List.of());

        assertThat(cancel.commandId()).isEqualTo(CommandId.CANCEL_SM);
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        CancelSm cancel = new CancelSm(123, "", "MSG", null, null, List.of());

        CancelSmResp resp = cancel.createResponse();

        assertThat(resp.sequenceNumber()).isEqualTo(123);
        assertThat(resp.commandStatus().isSuccess()).isTrue();
    }

    @Test
    @DisplayName("should create response with custom status")
    void shouldCreateResponseWithCustomStatus() {
        CancelSm cancel = new CancelSm(456, "", "MSG", null, null, List.of());

        CancelSmResp resp = cancel.createResponse(CommandStatus.ESME_RCANCELFAIL);

        assertThat(resp.sequenceNumber()).isEqualTo(456);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RCANCELFAIL);
    }

    @Test
    @DisplayName("should handle null serviceType as empty string")
    void shouldHandleNullServiceTypeAsEmptyString() {
        CancelSm cancel = new CancelSm(1, null, "MSG", null, null, List.of());

        assertThat(cancel.serviceType()).isEmpty();
    }

    @Test
    @DisplayName("should have immutable optional parameters")
    void shouldHaveImmutableOptionalParameters() {
        CancelSm cancel = new CancelSm(1, "", "", null, null, List.of());

        assertThatThrownBy(() -> cancel.optionalParameters().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should handle null optional parameters as empty list")
    void shouldHandleNullOptionalParametersAsEmptyList() {
        CancelSm cancel = new CancelSm(1, "", "", null, null, null);

        assertThat(cancel.optionalParameters()).isEmpty();
    }
}
