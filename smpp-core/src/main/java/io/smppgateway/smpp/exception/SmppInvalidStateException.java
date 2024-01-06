package io.smppgateway.smpp.exception;

import io.smppgateway.smpp.types.CommandStatus;

/**
 * Thrown when an operation is attempted in an invalid session state.
 */
public class SmppInvalidStateException extends SmppException {

    public SmppInvalidStateException(String message) {
        super(CommandStatus.ESME_RINVBNDSTS, message);
    }

    public SmppInvalidStateException(String message, Throwable cause) {
        super(CommandStatus.ESME_RINVBNDSTS, message, cause);
    }
}
