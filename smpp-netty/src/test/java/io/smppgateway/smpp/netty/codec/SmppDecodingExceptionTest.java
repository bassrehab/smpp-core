package io.smppgateway.smpp.netty.codec;

import io.smppgateway.smpp.exception.SmppException;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppDecodingException Tests")
class SmppDecodingExceptionTest {

    @Test
    @DisplayName("should create exception with message")
    void shouldCreateExceptionWithMessage() {
        SmppDecodingException ex = new SmppDecodingException("Invalid PDU format");

        assertThat(ex.getMessage()).isEqualTo("Invalid PDU format");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("Buffer underflow");
        SmppDecodingException ex = new SmppDecodingException("Failed to decode PDU", cause);

        assertThat(ex.getMessage()).isEqualTo("Failed to decode PDU");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("should extend SmppException")
    void shouldExtendSmppException() {
        SmppDecodingException ex = new SmppDecodingException("test");

        assertThat(ex).isInstanceOf(SmppException.class);
    }

    @Test
    @DisplayName("should have ESME_RSYSERR command status")
    void shouldHaveSystemErrorCommandStatus() {
        SmppDecodingException ex = new SmppDecodingException("test");

        assertThat(ex.commandStatus()).isEqualTo(CommandStatus.ESME_RSYSERR);
    }
}
