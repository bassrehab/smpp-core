package io.smppgateway.smpp.exception;

import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppException Tests")
class SmppExceptionTest {

    @Test
    @DisplayName("should create exception with message only")
    void shouldCreateExceptionWithMessageOnly() {
        SmppException ex = new SmppException("Test error");

        assertThat(ex.getMessage()).isEqualTo("Test error");
        assertThat(ex.commandStatus()).isEqualTo(CommandStatus.ESME_RSYSERR);
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        SmppException ex = new SmppException("Test error", cause);

        assertThat(ex.getMessage()).isEqualTo("Test error");
        assertThat(ex.commandStatus()).isEqualTo(CommandStatus.ESME_RSYSERR);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("should create exception with custom status and message")
    void shouldCreateExceptionWithCustomStatusAndMessage() {
        SmppException ex = new SmppException(CommandStatus.ESME_RTHROTTLED, "Rate limited");

        assertThat(ex.getMessage()).isEqualTo("Rate limited");
        assertThat(ex.commandStatus()).isEqualTo(CommandStatus.ESME_RTHROTTLED);
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("should create exception with custom status message and cause")
    void shouldCreateExceptionWithCustomStatusMessageAndCause() {
        RuntimeException cause = new RuntimeException("Connection failed");
        SmppException ex = new SmppException(CommandStatus.ESME_RBINDFAIL, "Bind failed", cause);

        assertThat(ex.getMessage()).isEqualTo("Bind failed");
        assertThat(ex.commandStatus()).isEqualTo(CommandStatus.ESME_RBINDFAIL);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("should be a RuntimeException")
    void shouldBeRuntimeException() {
        SmppException ex = new SmppException("Test");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("should preserve different command statuses")
    void shouldPreserveDifferentCommandStatuses() {
        assertThat(new SmppException(CommandStatus.ESME_RINVPASWD, "bad password").commandStatus())
                .isEqualTo(CommandStatus.ESME_RINVPASWD);
        assertThat(new SmppException(CommandStatus.ESME_RINVCMDID, "invalid command").commandStatus())
                .isEqualTo(CommandStatus.ESME_RINVCMDID);
        assertThat(new SmppException(CommandStatus.ESME_RINVMSGLEN, "invalid length").commandStatus())
                .isEqualTo(CommandStatus.ESME_RINVMSGLEN);
    }
}
