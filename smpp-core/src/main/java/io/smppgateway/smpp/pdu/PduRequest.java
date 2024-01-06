package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.CommandStatus;

/**
 * Sealed interface for all SMPP request PDUs.
 * <p>
 * Request PDUs are sent by ESMEs (clients) or SMSCs (servers) and expect
 * a corresponding response PDU with the same sequence number.
 *
 * @param <R> the type of response PDU expected
 */
public sealed interface PduRequest<R extends PduResponse> extends Pdu
        permits BindTransmitter, BindReceiver, BindTransceiver,
                SubmitSm, DeliverSm, DataSm, QuerySm, CancelSm, ReplaceSm,
                EnquireLink, Unbind, Outbind, AlertNotification, SubmitMulti {

    /**
     * Request PDUs always have OK status.
     */
    @Override
    default CommandStatus commandStatus() {
        return CommandStatus.OK;
    }

    /**
     * Creates a success response for this request.
     *
     * @return the response PDU with OK status
     */
    R createResponse();

    /**
     * Creates a response with the specified status.
     *
     * @param status the command status
     * @return the response PDU
     */
    R createResponse(CommandStatus status);

    /**
     * Creates a response with an error status and message ID (for submit_sm_resp).
     * Default implementation delegates to createResponse(status).
     *
     * @param status    the command status
     * @param messageId optional message ID (may be null)
     * @return the response PDU
     */
    default R createResponse(CommandStatus status, String messageId) {
        return createResponse(status);
    }
}
