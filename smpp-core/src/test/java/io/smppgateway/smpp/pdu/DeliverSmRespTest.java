package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DeliverSmResp Tests")
class DeliverSmRespTest {

    @Test
    @DisplayName("should create response with all fields")
    void shouldCreateResponseWithAllFields() {
        DeliverSmResp resp = new DeliverSmResp(123, CommandStatus.OK, "MSG789", List.of());

        assertThat(resp.sequenceNumber()).isEqualTo(123);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.OK);
        assertThat(resp.messageId()).isEqualTo("MSG789");
        assertThat(resp.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        DeliverSmResp resp = new DeliverSmResp(1, CommandStatus.OK, "", List.of());

        assertThat(resp.commandId()).isEqualTo(CommandId.DELIVER_SM_RESP);
    }

    @Test
    @DisplayName("should handle null optional parameters")
    void shouldHandleNullOptionalParameters() {
        DeliverSmResp resp = new DeliverSmResp(1, CommandStatus.OK, "MSG", null);

        assertThat(resp.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should have immutable optional parameters")
    void shouldHaveImmutableOptionalParameters() {
        DeliverSmResp resp = new DeliverSmResp(1, CommandStatus.OK, "MSG", List.of());

        assertThatThrownBy(() -> resp.optionalParameters().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should be a PduResponse")
    void shouldBePduResponse() {
        DeliverSmResp resp = new DeliverSmResp(1, CommandStatus.OK, "", List.of());

        assertThat(resp).isInstanceOf(PduResponse.class);
    }

    @Test
    @DisplayName("should create error response without message ID")
    void shouldCreateErrorResponseWithoutMessageId() {
        DeliverSmResp resp = new DeliverSmResp(456, CommandStatus.ESME_RDELIVERYFAILURE, null, List.of());

        assertThat(resp.sequenceNumber()).isEqualTo(456);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RDELIVERYFAILURE);
        assertThat(resp.messageId()).isNull();
    }
}
