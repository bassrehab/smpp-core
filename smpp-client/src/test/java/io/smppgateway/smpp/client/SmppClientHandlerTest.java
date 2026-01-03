package io.smppgateway.smpp.client;

import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppClientHandler Tests")
class SmppClientHandlerTest {

    @Nested
    @DisplayName("DeliverSmResult Tests")
    class DeliverSmResultTests {

        @Test
        @DisplayName("should create success result without message ID")
        void shouldCreateSuccessResultWithoutMessageId() {
            SmppClientHandler.DeliverSmResult result = SmppClientHandler.DeliverSmResult.success();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEmpty();
        }

        @Test
        @DisplayName("should create success result with message ID")
        void shouldCreateSuccessResultWithMessageId() {
            SmppClientHandler.DeliverSmResult result = SmppClientHandler.DeliverSmResult.success("MSG001");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("MSG001");
        }

        @Test
        @DisplayName("should create failure result")
        void shouldCreateFailureResult() {
            SmppClientHandler.DeliverSmResult result = SmppClientHandler.DeliverSmResult.failure(CommandStatus.ESME_RDELIVERYFAILURE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_RDELIVERYFAILURE);
            assertThat(result.messageId()).isEmpty();
        }

        @Test
        @DisplayName("should allow creating result directly")
        void shouldAllowCreatingResultDirectly() {
            SmppClientHandler.DeliverSmResult result = new SmppClientHandler.DeliverSmResult(CommandStatus.ESME_ROK, "MSG123");

            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("MSG123");
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("isSuccess should return false for non-OK status")
        void isSuccessShouldReturnFalseForNonOkStatus() {
            SmppClientHandler.DeliverSmResult result = new SmppClientHandler.DeliverSmResult(
                    CommandStatus.ESME_RINVMSGID, "MSG");

            assertThat(result.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("DataSmResult Tests")
    class DataSmResultTests {

        @Test
        @DisplayName("should create success result without message ID")
        void shouldCreateSuccessResultWithoutMessageId() {
            SmppClientHandler.DataSmResult result = SmppClientHandler.DataSmResult.success();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEmpty();
        }

        @Test
        @DisplayName("should create success result with message ID")
        void shouldCreateSuccessResultWithMessageId() {
            SmppClientHandler.DataSmResult result = SmppClientHandler.DataSmResult.success("DATA001");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("DATA001");
        }

        @Test
        @DisplayName("should create failure result")
        void shouldCreateFailureResult() {
            SmppClientHandler.DataSmResult result = SmppClientHandler.DataSmResult.failure(CommandStatus.ESME_RINVCMDID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.status()).isEqualTo(CommandStatus.ESME_RINVCMDID);
            assertThat(result.messageId()).isEmpty();
        }

        @Test
        @DisplayName("should allow creating result directly")
        void shouldAllowCreatingResultDirectly() {
            SmppClientHandler.DataSmResult result = new SmppClientHandler.DataSmResult(CommandStatus.ESME_ROK, "DATA123");

            assertThat(result.status()).isEqualTo(CommandStatus.ESME_ROK);
            assertThat(result.messageId()).isEqualTo("DATA123");
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("isSuccess should return false for non-OK status")
        void isSuccessShouldReturnFalseForNonOkStatus() {
            SmppClientHandler.DataSmResult result = new SmppClientHandler.DataSmResult(
                    CommandStatus.ESME_RSYSERR, "DATA");

            assertThat(result.isSuccess()).isFalse();
        }
    }
}
