package io.smppgateway.smpp.pdu;

/**
 * Sealed interface for all SMPP response PDUs.
 * <p>
 * Response PDUs are sent in reply to request PDUs and share the same
 * sequence number as the original request. The command_id has the high
 * bit set (0x80000000 OR'd with the request command_id).
 */
public sealed interface PduResponse extends Pdu
        permits BindTransmitterResp, BindReceiverResp, BindTransceiverResp,
                SubmitSmResp, DeliverSmResp, DataSmResp, QuerySmResp,
                CancelSmResp, ReplaceSmResp, EnquireLinkResp, UnbindResp,
                SubmitMultiResp {

    /**
     * Returns true if this response indicates success.
     */
    default boolean isSuccess() {
        return commandStatus().isSuccess();
    }

    /**
     * Returns true if this response indicates an error.
     */
    default boolean isError() {
        return commandStatus().isError();
    }
}
