package io.smppgateway.smpp.server;

import io.smppgateway.smpp.pdu.SubmitMultiResp;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppServerHandler Tests")
class SmppServerHandlerTest {

    @Nested
    @DisplayName("BindResult Tests")
    class BindResultTests {

        @Test
        @DisplayName("should create success result without system ID")
        void shouldCreateSuccessResultWithoutSystemId() {
            SmppServerHandler.BindResult result = SmppServerHandler.BindResult.success();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.systemId()).isNull();
        }

        @Test
        @DisplayName("should create success result with system ID")
        void shouldCreateSuccessResultWithSystemId() {
            SmppServerHandler.BindResult result = SmppServerHandler.BindResult.success("SMSC");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.systemId()).isEqualTo("SMSC");
        }

        @Test
        @DisplayName("should create failure result")
        void shouldCreateFailureResult() {
            SmppServerHandler.BindResult result = SmppServerHandler.BindResult.failure(CommandStatus.ESME_RINVPASWD);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_RINVPASWD);
            assertThat(result.systemId()).isNull();
        }

        @Test
        @DisplayName("should allow creating result directly")
        void shouldAllowCreatingResultDirectly() {
            SmppServerHandler.BindResult result = new SmppServerHandler.BindResult(CommandStatus.ESME_RBINDFAIL, "test");

            assertThat(result.status()).isEqualTo(CommandStatus.ESME_RBINDFAIL);
            assertThat(result.systemId()).isEqualTo("test");
            assertThat(result.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("SubmitSmResult Tests")
    class SubmitSmResultTests {

        @Test
        @DisplayName("should create success result with message ID")
        void shouldCreateSuccessResultWithMessageId() {
            SmppServerHandler.SubmitSmResult result = SmppServerHandler.SubmitSmResult.success("MSG001");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("MSG001");
        }

        @Test
        @DisplayName("should create failure result")
        void shouldCreateFailureResult() {
            SmppServerHandler.SubmitSmResult result = SmppServerHandler.SubmitSmResult.failure(CommandStatus.ESME_RSUBMITFAIL);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_RSUBMITFAIL);
            assertThat(result.messageId()).isNull();
        }

        @Test
        @DisplayName("should allow creating result directly")
        void shouldAllowCreatingResultDirectly() {
            SmppServerHandler.SubmitSmResult result = new SmppServerHandler.SubmitSmResult(CommandStatus.ESME_ROK, "MSG123");

            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("MSG123");
            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("DataSmResult Tests")
    class DataSmResultTests {

        @Test
        @DisplayName("should create success result with message ID")
        void shouldCreateSuccessResultWithMessageId() {
            SmppServerHandler.DataSmResult result = SmppServerHandler.DataSmResult.success("DATA001");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("DATA001");
        }

        @Test
        @DisplayName("should create failure result")
        void shouldCreateFailureResult() {
            SmppServerHandler.DataSmResult result = SmppServerHandler.DataSmResult.failure(CommandStatus.ESME_RINVCMDID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_RINVCMDID);
            assertThat(result.messageId()).isNull();
        }

        @Test
        @DisplayName("should allow creating result directly")
        void shouldAllowCreatingResultDirectly() {
            SmppServerHandler.DataSmResult result = new SmppServerHandler.DataSmResult(CommandStatus.ESME_ROK, "DATA123");

            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("DATA123");
            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("QuerySmResult Tests")
    class QuerySmResultTests {

        @Test
        @DisplayName("should create success result with all fields")
        void shouldCreateSuccessResultWithAllFields() {
            SmppServerHandler.QuerySmResult result = SmppServerHandler.QuerySmResult.success(
                    "MSG001", "231215120000000+", (byte) 2, (byte) 0);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("MSG001");
            assertThat(result.finalDate()).isEqualTo("231215120000000+");
            assertThat(result.messageState()).isEqualTo((byte) 2);
            assertThat(result.errorCode()).isEqualTo((byte) 0);
        }

        @Test
        @DisplayName("should create failure result")
        void shouldCreateFailureResult() {
            SmppServerHandler.QuerySmResult result = SmppServerHandler.QuerySmResult.failure(CommandStatus.ESME_RQUERYFAIL);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_RQUERYFAIL);
            assertThat(result.messageId()).isNull();
            assertThat(result.finalDate()).isNull();
            assertThat(result.messageState()).isEqualTo((byte) 0);
            assertThat(result.errorCode()).isEqualTo((byte) 0);
        }

        @Test
        @DisplayName("should allow creating result directly")
        void shouldAllowCreatingResultDirectly() {
            SmppServerHandler.QuerySmResult result = new SmppServerHandler.QuerySmResult(
                    CommandStatus.ESME_ROK, "MSG", "date", (byte) 1, (byte) 2);

            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("MSG");
            assertThat(result.finalDate()).isEqualTo("date");
            assertThat(result.messageState()).isEqualTo((byte) 1);
            assertThat(result.errorCode()).isEqualTo((byte) 2);
        }
    }

    @Nested
    @DisplayName("SubmitMultiResult Tests")
    class SubmitMultiResultTests {

        @Test
        @DisplayName("should create success result with message ID only")
        void shouldCreateSuccessResultWithMessageIdOnly() {
            SmppServerHandler.SubmitMultiResult result = SmppServerHandler.SubmitMultiResult.success("MULTI001");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("MULTI001");
            assertThat(result.failures()).isEmpty();
        }

        @Test
        @DisplayName("should create success result with failures list")
        void shouldCreateSuccessResultWithFailuresList() {
            Address addr = Address.international("+14155551234");
            SubmitMultiResp.UnsuccessfulDelivery failure =
                    new SubmitMultiResp.UnsuccessfulDelivery(addr, CommandStatus.ESME_RINVDSTADR);
            List<SubmitMultiResp.UnsuccessfulDelivery> failures = List.of(failure);

            SmppServerHandler.SubmitMultiResult result = SmppServerHandler.SubmitMultiResult.success("MULTI001", failures);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("MULTI001");
            assertThat(result.failures()).hasSize(1);
            assertThat(result.failures().get(0).address()).isEqualTo(addr);
        }

        @Test
        @DisplayName("should create failure result")
        void shouldCreateFailureResult() {
            SmppServerHandler.SubmitMultiResult result = SmppServerHandler.SubmitMultiResult.failure(CommandStatus.ESME_RSUBMITFAIL);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_RSUBMITFAIL);
            assertThat(result.messageId()).isNull();
            assertThat(result.failures()).isEmpty();
        }

        @Test
        @DisplayName("should allow creating result directly")
        void shouldAllowCreatingResultDirectly() {
            SmppServerHandler.SubmitMultiResult result = new SmppServerHandler.SubmitMultiResult(
                    CommandStatus.ESME_ROK, "MSG123", List.of());

            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("MSG123");
            assertThat(result.failures()).isEmpty();
            assertThat(result.isSuccess()).isTrue();
        }
    }
}
