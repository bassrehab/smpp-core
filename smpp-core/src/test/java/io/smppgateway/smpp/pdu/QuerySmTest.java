package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("QuerySm Tests")
class QuerySmTest {

    @Test
    @DisplayName("should create QuerySm with required fields")
    void shouldCreateQuerySmWithRequiredFields() {
        Address source = Address.international("+14155551234");
        QuerySm query = new QuerySm(1, "MSG123", source, List.of());

        assertThat(query.sequenceNumber()).isEqualTo(1);
        assertThat(query.messageId()).isEqualTo("MSG123");
        assertThat(query.sourceAddress()).isEqualTo(source);
        assertThat(query.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        QuerySm query = new QuerySm(1, "MSG", Address.international("+1234"), List.of());

        assertThat(query.commandId()).isEqualTo(CommandId.QUERY_SM);
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        QuerySm query = new QuerySm(123, "MSG456", Address.international("+1234"), List.of());

        QuerySmResp resp = query.createResponse();

        assertThat(resp.sequenceNumber()).isEqualTo(123);
        assertThat(resp.commandStatus().isSuccess()).isTrue();
        assertThat(resp.messageId()).isEqualTo("MSG456");
    }

    @Test
    @DisplayName("should create response with custom status")
    void shouldCreateResponseWithCustomStatus() {
        QuerySm query = new QuerySm(456, "MSG789", Address.international("+1234"), List.of());

        QuerySmResp resp = query.createResponse(CommandStatus.ESME_RQUERYFAIL);

        assertThat(resp.sequenceNumber()).isEqualTo(456);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RQUERYFAIL);
    }

    @Test
    @DisplayName("should throw exception when messageId is null")
    void shouldThrowExceptionWhenMessageIdIsNull() {
        Address source = Address.international("+1234");

        assertThatNullPointerException()
                .isThrownBy(() -> new QuerySm(1, null, source, List.of()))
                .withMessage("messageId required");
    }

    @Test
    @DisplayName("should throw exception when sourceAddress is null")
    void shouldThrowExceptionWhenSourceAddressIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new QuerySm(1, "MSG", null, List.of()))
                .withMessage("sourceAddress required");
    }

    @Test
    @DisplayName("should have immutable optional parameters")
    void shouldHaveImmutableOptionalParameters() {
        QuerySm query = new QuerySm(1, "MSG", Address.international("+1234"), List.of());

        assertThatThrownBy(() -> query.optionalParameters().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should handle null optional parameters as empty list")
    void shouldHandleNullOptionalParametersAsEmptyList() {
        QuerySm query = new QuerySm(1, "MSG", Address.international("+1234"), null);

        assertThat(query.optionalParameters()).isEmpty();
    }
}
