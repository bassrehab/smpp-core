package io.smppgateway.smpp.exception;

import io.smppgateway.smpp.types.CommandStatus;

/**
 * Base exception for all SMPP-related errors.
 */
public class SmppException extends RuntimeException {

    private final CommandStatus commandStatus;

    public SmppException(String message) {
        super(message);
        this.commandStatus = CommandStatus.ESME_RSYSERR;
    }

    public SmppException(String message, Throwable cause) {
        super(message, cause);
        this.commandStatus = CommandStatus.ESME_RSYSERR;
    }

    public SmppException(CommandStatus status, String message) {
        super(message);
        this.commandStatus = status;
    }

    public SmppException(CommandStatus status, String message, Throwable cause) {
        super(message, cause);
        this.commandStatus = status;
    }

    /**
     * Returns the SMPP command status associated with this error.
     */
    public CommandStatus commandStatus() {
        return commandStatus;
    }
}
