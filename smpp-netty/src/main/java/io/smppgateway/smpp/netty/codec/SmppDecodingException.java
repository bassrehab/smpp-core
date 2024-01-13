package io.smppgateway.smpp.netty.codec;

import io.smppgateway.smpp.exception.SmppException;

/**
 * Thrown when PDU decoding fails due to malformed data.
 */
public class SmppDecodingException extends SmppException {

    public SmppDecodingException(String message) {
        super(message);
    }

    public SmppDecodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
