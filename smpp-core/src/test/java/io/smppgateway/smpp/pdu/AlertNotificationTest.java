package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.pdu.tlv.Tlv;
import io.smppgateway.smpp.pdu.tlv.TlvTag;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AlertNotification Tests")
class AlertNotificationTest {

    @Test
    @DisplayName("should create AlertNotification with all fields")
    void shouldCreateAlertNotificationWithAllFields() {
        Address source = Address.international("+14155551234");
        Address esme = Address.alphanumeric("MYAPP");

        AlertNotification alert = new AlertNotification(1, source, esme, List.of());

        assertThat(alert.sequenceNumber()).isEqualTo(1);
        assertThat(alert.sourceAddress()).isEqualTo(source);
        assertThat(alert.esmeAddress()).isEqualTo(esme);
        assertThat(alert.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        AlertNotification alert = new AlertNotification(1, null, null, List.of());

        assertThat(alert.commandId()).isEqualTo(CommandId.ALERT_NOTIFICATION);
    }

    @Test
    @DisplayName("should create response with OK status")
    void shouldCreateResponseWithOkStatus() {
        AlertNotification alert = new AlertNotification(123, null, null, List.of());

        // AlertNotification creates EnquireLinkResp as it doesn't have a dedicated response
        EnquireLinkResp resp = alert.createResponse();

        assertThat(resp.sequenceNumber()).isEqualTo(123);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.OK);
    }

    @Test
    @DisplayName("should create response with custom status")
    void shouldCreateResponseWithCustomStatus() {
        AlertNotification alert = new AlertNotification(456, null, null, List.of());

        EnquireLinkResp resp = alert.createResponse(CommandStatus.ESME_RSYSERR);

        assertThat(resp.sequenceNumber()).isEqualTo(456);
        assertThat(resp.commandStatus()).isEqualTo(CommandStatus.ESME_RSYSERR);
    }

    @Test
    @DisplayName("should support optional parameters")
    void shouldSupportOptionalParameters() {
        Tlv msAvailStatus = Tlv.ofByte(TlvTag.MS_AVAILABILITY_STATUS, (byte) 1);
        AlertNotification alert = new AlertNotification(1, null, null, List.of(msAvailStatus));

        assertThat(alert.optionalParameters()).hasSize(1);
    }

    @Test
    @DisplayName("should have immutable optional parameters")
    void shouldHaveImmutableOptionalParameters() {
        AlertNotification alert = new AlertNotification(1, null, null, List.of());

        assertThatThrownBy(() -> alert.optionalParameters().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should handle null optional parameters as empty list")
    void shouldHandleNullOptionalParametersAsEmptyList() {
        AlertNotification alert = new AlertNotification(1, null, null, null);

        assertThat(alert.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should be a PduRequest")
    void shouldBePduRequest() {
        AlertNotification alert = new AlertNotification(1, null, null, List.of());

        assertThat(alert).isInstanceOf(PduRequest.class);
    }
}
