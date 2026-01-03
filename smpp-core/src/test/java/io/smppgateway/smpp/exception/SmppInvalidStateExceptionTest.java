package io.smppgateway.smpp.exception;

import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppInvalidStateException Tests")
class SmppInvalidStateExceptionTest {

    @Test
    @DisplayName("should create exception with message")
    void shouldCreateExceptionWithMessage() {
        SmppInvalidStateException ex = new SmppInvalidStateException("Session not bound");

        assertThat(ex.getMessage()).isEqualTo("Session not bound");
        assertThat(ex.commandStatus()).isEqualTo(CommandStatus.ESME_RINVBNDSTS);
    }

    @Test
    @DisplayName("should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("State error");
        SmppInvalidStateException ex = new SmppInvalidStateException("Invalid state transition", cause);

        assertThat(ex.getMessage()).isEqualTo("Invalid state transition");
        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.commandStatus()).isEqualTo(CommandStatus.ESME_RINVBNDSTS);
    }

    @Test
    @DisplayName("should extend SmppException")
    void shouldExtendSmppException() {
        SmppInvalidStateException ex = new SmppInvalidStateException("test");

        assertThat(ex).isInstanceOf(SmppException.class);
    }

    @Test
    @DisplayName("should always use ESME_RINVBNDSTS status")
    void shouldAlwaysUseInvalidBindStatus() {
        SmppInvalidStateException ex1 = new SmppInvalidStateException("test1");
        SmppInvalidStateException ex2 = new SmppInvalidStateException("test2", new RuntimeException());

        assertThat(ex1.commandStatus()).isEqualTo(CommandStatus.ESME_RINVBNDSTS);
        assertThat(ex2.commandStatus()).isEqualTo(CommandStatus.ESME_RINVBNDSTS);
    }
}
