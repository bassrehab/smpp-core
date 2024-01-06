package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;
import java.util.Objects;

/**
 * Query Short Message PDU - queries the status of a previously submitted message.
 */
public record QuerySm(
        int sequenceNumber,
        String messageId,
        Address sourceAddress,
        List<Tlv> optionalParameters
) implements PduRequest<QuerySmResp> {

    public QuerySm {
        Objects.requireNonNull(messageId, "messageId required");
        Objects.requireNonNull(sourceAddress, "sourceAddress required");
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.QUERY_SM;
    }

    @Override
    public QuerySmResp createResponse() {
        return createResponse(CommandStatus.OK);
    }

    @Override
    public QuerySmResp createResponse(CommandStatus status) {
        return new QuerySmResp(sequenceNumber, status, messageId, "", (byte) 0, (byte) 0, List.of());
    }
}
