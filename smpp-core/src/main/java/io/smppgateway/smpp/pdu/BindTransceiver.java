package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.util.List;
import java.util.Objects;

/**
 * Bind Transceiver PDU - establishes a bidirectional session.
 */
public record BindTransceiver(
        int sequenceNumber,
        String systemId,
        String password,
        String systemType,
        byte interfaceVersion,
        byte addrTon,
        byte addrNpi,
        String addressRange,
        List<Tlv> optionalParameters
) implements PduRequest<BindTransceiverResp> {

    public BindTransceiver {
        Objects.requireNonNull(systemId, "systemId required");
        Objects.requireNonNull(password, "password required");
        systemType = systemType != null ? systemType : "";
        addressRange = addressRange != null ? addressRange : "";
        optionalParameters = optionalParameters != null ? List.copyOf(optionalParameters) : List.of();
    }

    @Override
    public CommandId commandId() {
        return CommandId.BIND_TRANSCEIVER;
    }

    @Override
    public BindTransceiverResp createResponse() {
        return createResponse(CommandStatus.OK);
    }

    @Override
    public BindTransceiverResp createResponse(CommandStatus status) {
        return new BindTransceiverResp(sequenceNumber, status, systemId, List.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int sequenceNumber;
        private String systemId;
        private String password;
        private String systemType = "";
        private byte interfaceVersion = BindTransmitter.SMPP_VERSION_3_4;
        private byte addrTon;
        private byte addrNpi;
        private String addressRange = "";

        public Builder sequenceNumber(int seq) { this.sequenceNumber = seq; return this; }
        public Builder systemId(String id) { this.systemId = id; return this; }
        public Builder password(String pwd) { this.password = pwd; return this; }
        public Builder systemType(String type) { this.systemType = type; return this; }
        public Builder interfaceVersion(byte ver) { this.interfaceVersion = ver; return this; }
        public Builder addrTon(byte ton) { this.addrTon = ton; return this; }
        public Builder addrNpi(byte npi) { this.addrNpi = npi; return this; }
        public Builder addressRange(String range) { this.addressRange = range; return this; }

        public BindTransceiver build() {
            return new BindTransceiver(sequenceNumber, systemId, password, systemType,
                    interfaceVersion, addrTon, addrNpi, addressRange, List.of());
        }
    }
}
