package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.pdu.tlv.TlvTag;
import io.smppgateway.smpp.types.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DataSm Tests")
class DataSmTest {

    @Test
    @DisplayName("should create DataSm with all fields")
    void shouldCreateDataSmWithAllFields() {
        Address source = Address.international("+14155551234");
        Address dest = Address.international("+14155555678");

        DataSm dataSm = new DataSm(1, "WAP", source, dest,
                EsmClass.DEFAULT, RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED,
                DataCoding.UCS2, List.of());

        assertThat(dataSm.sequenceNumber()).isEqualTo(1);
        assertThat(dataSm.serviceType()).isEqualTo("WAP");
        assertThat(dataSm.sourceAddress()).isEqualTo(source);
        assertThat(dataSm.destAddress()).isEqualTo(dest);
        assertThat(dataSm.esmClass()).isEqualTo(EsmClass.DEFAULT);
        assertThat(dataSm.registeredDelivery()).isEqualTo(RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED);
        assertThat(dataSm.dataCoding()).isEqualTo(DataCoding.UCS2);
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        DataSm dataSm = new DataSm(1, "", Address.international("+1"), Address.international("+2"),
                null, null, null, List.of());

        assertThat(dataSm.commandId()).isEqualTo(CommandId.DATA_SM);
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        DataSm dataSm = new DataSm(123, "", Address.international("+1"), Address.international("+2"),
                null, null, null, List.of());

        DataSmResp resp = dataSm.createResponse();

        assertThat(resp.sequenceNumber()).isEqualTo(123);
        assertThat(resp.commandStatus().isSuccess()).isTrue();
    }

    @Test
    @DisplayName("should create response with custom status")
    void shouldCreateResponseWithCustomStatus() {
        DataSm dataSm = new DataSm(456, "", Address.international("+1"), Address.international("+2"),
                null, null, null, List.of());

        DataSmResp resp = dataSm.createResponse(CommandStatus.ESME_RSUBMITFAIL);

        assertThat(resp.sequenceNumber()).isEqualTo(456);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RSUBMITFAIL);
    }

    @Test
    @DisplayName("should throw exception when sourceAddress is null")
    void shouldThrowExceptionWhenSourceAddressIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new DataSm(1, "", null, Address.international("+1"),
                        null, null, null, List.of()))
                .withMessage("sourceAddress required");
    }

    @Test
    @DisplayName("should throw exception when destAddress is null")
    void shouldThrowExceptionWhenDestAddressIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new DataSm(1, "", Address.international("+1"), null,
                        null, null, null, List.of()))
                .withMessage("destAddress required");
    }

    @Test
    @DisplayName("should use default values for optional fields")
    void shouldUseDefaultValuesForOptionalFields() {
        DataSm dataSm = new DataSm(1, null, Address.international("+1"), Address.international("+2"),
                null, null, null, null);

        assertThat(dataSm.serviceType()).isEmpty();
        assertThat(dataSm.esmClass()).isEqualTo(EsmClass.DEFAULT);
        assertThat(dataSm.registeredDelivery()).isEqualTo(RegisteredDelivery.NONE);
        assertThat(dataSm.dataCoding()).isEqualTo(DataCoding.DEFAULT);
        assertThat(dataSm.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should support optional parameters with message payload")
    void shouldSupportOptionalParametersWithMessagePayload() {
        Tlv payload = Tlv.ofString(TlvTag.MESSAGE_PAYLOAD, "Hello via DataSm");

        DataSm dataSm = new DataSm(1, "", Address.international("+1"), Address.international("+2"),
                null, null, null, List.of(payload));

        assertThat(dataSm.optionalParameters()).hasSize(1);
        assertThat(dataSm.optionalParameters().get(0).knownTag()).isEqualTo(TlvTag.MESSAGE_PAYLOAD);
    }

    @Test
    @DisplayName("should have immutable optional parameters")
    void shouldHaveImmutableOptionalParameters() {
        DataSm dataSm = new DataSm(1, "", Address.international("+1"), Address.international("+2"),
                null, null, null, List.of());

        assertThatThrownBy(() -> dataSm.optionalParameters().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
