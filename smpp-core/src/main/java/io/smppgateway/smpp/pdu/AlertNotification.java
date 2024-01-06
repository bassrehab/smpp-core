package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;

/**
 * Alert Notification PDU - sent by SMSC to alert an ESME that an MS is available.
 * This PDU does not require a response.
 */
public record AlertNotification(
        int sequenceNumber,
        Address sourceAddress,
        Address esmeAddress,
        List<Tlv> optionalParameters
) implements PduRequest<EnquireLinkResp> {

    public AlertNotification {
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.ALERT_NOTIFICATION;
    }

    @Override
    public EnquireLinkResp createResponse() {
        // Alert notifications don't require responses, but interface requires implementation
        return new EnquireLinkResp(sequenceNumber, CommandStatus.OK);
    }

    @Override
    public EnquireLinkResp createResponse(CommandStatus status) {
        return new EnquireLinkResp(sequenceNumber, status);
    }
}
