package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import io.smppgateway.smpp.types.RegisteredDelivery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReplaceSm Tests")
class ReplaceSmTest {

    @Test
    @DisplayName("should create ReplaceSm with all fields")
    void shouldCreateReplaceSmWithAllFields() {
        Address source = Address.international("+14155551234");
        byte[] message = "New message".getBytes(StandardCharsets.UTF_8);

        ReplaceSm replace = new ReplaceSm(1, "MSG123", source,
                "210101120000000R", "210102120000000R",
                RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED,
                (byte) 0, message, List.of());

        assertThat(replace.sequenceNumber()).isEqualTo(1);
        assertThat(replace.messageId()).isEqualTo("MSG123");
        assertThat(replace.sourceAddress()).isEqualTo(source);
        assertThat(replace.scheduleDeliveryTime()).isEqualTo("210101120000000R");
        assertThat(replace.validityPeriod()).isEqualTo("210102120000000R");
        assertThat(replace.registeredDelivery()).isEqualTo(RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED);
        assertThat(replace.smDefaultMsgId()).isZero();
        assertThat(replace.shortMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        ReplaceSm replace = new ReplaceSm(1, "", null, "", "", null, (byte) 0, null, List.of());

        assertThat(replace.commandId()).isEqualTo(CommandId.REPLACE_SM);
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        ReplaceSm replace = new ReplaceSm(123, "", null, "", "", null, (byte) 0, null, List.of());

        ReplaceSmResp resp = replace.createResponse();

        assertThat(resp.sequenceNumber()).isEqualTo(123);
        assertThat(resp.commandStatus().isSuccess()).isTrue();
    }

    @Test
    @DisplayName("should create response with custom status")
    void shouldCreateResponseWithCustomStatus() {
        ReplaceSm replace = new ReplaceSm(456, "", null, "", "", null, (byte) 0, null, List.of());

        ReplaceSmResp resp = replace.createResponse(CommandStatus.ESME_RREPLACEFAIL);

        assertThat(resp.sequenceNumber()).isEqualTo(456);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RREPLACEFAIL);
    }

    @Test
    @DisplayName("should use default values for optional fields")
    void shouldUseDefaultValuesForOptionalFields() {
        ReplaceSm replace = new ReplaceSm(1, "", null, null, null, null, (byte) 0, null, null);

        assertThat(replace.scheduleDeliveryTime()).isEmpty();
        assertThat(replace.validityPeriod()).isEmpty();
        assertThat(replace.registeredDelivery()).isEqualTo(RegisteredDelivery.NONE);
        assertThat(replace.shortMessage()).isEmpty();
        assertThat(replace.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should clone short message array")
    void shouldCloneShortMessageArray() {
        byte[] original = "Test".getBytes();
        ReplaceSm replace = new ReplaceSm(1, "", null, "", "", null, (byte) 0, original, List.of());

        // Modify original array
        original[0] = 'X';

        // Verify the ReplaceSm's array is not affected
        assertThat(replace.shortMessage()[0]).isEqualTo((byte) 'T');
    }

    @Test
    @DisplayName("should have immutable optional parameters")
    void shouldHaveImmutableOptionalParameters() {
        ReplaceSm replace = new ReplaceSm(1, "", null, "", "", null, (byte) 0, null, List.of());

        assertThatThrownBy(() -> replace.optionalParameters().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should be a PduRequest")
    void shouldBePduRequest() {
        ReplaceSm replace = new ReplaceSm(1, "", null, "", "", null, (byte) 0, null, List.of());

        assertThat(replace).isInstanceOf(PduRequest.class);
    }
}
