package io.smppgateway.smpp.types;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CommandStatus Tests")
class CommandStatusTest {

    @Test
    @DisplayName("should have correct code for success status")
    void shouldHaveCorrectCodeForSuccess() {
        assertThat(CommandStatus.OK.code()).isEqualTo(0x00000000);
        assertThat(CommandStatus.ESME_ROK.code()).isEqualTo(0x00000000);
    }

    @Test
    @DisplayName("OK and ESME_ROK should be equivalent")
    void okAndEsmeRokShouldBeEquivalent() {
        assertThat(CommandStatus.OK.code()).isEqualTo(CommandStatus.ESME_ROK.code());
    }

    @Test
    @DisplayName("should correctly identify success status")
    void shouldIdentifySuccessStatus() {
        assertThat(CommandStatus.OK.isSuccess()).isTrue();
        assertThat(CommandStatus.ESME_ROK.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("should correctly identify error status")
    void shouldIdentifyErrorStatus() {
        assertThat(CommandStatus.ESME_RINVMSGLEN.isError()).isTrue();
        assertThat(CommandStatus.ESME_RSYSERR.isError()).isTrue();
        assertThat(CommandStatus.ESME_RBINDFAIL.isError()).isTrue();
        assertThat(CommandStatus.ESME_RTHROTTLED.isError()).isTrue();
    }

    @Test
    @DisplayName("success status should not be error")
    void successShouldNotBeError() {
        assertThat(CommandStatus.OK.isError()).isFalse();
        assertThat(CommandStatus.ESME_ROK.isError()).isFalse();
    }

    @Test
    @DisplayName("should find status by code")
    void shouldFindStatusByCode() {
        assertThat(CommandStatus.fromCode(0x00000000)).isEqualTo(CommandStatus.OK);
        assertThat(CommandStatus.fromCode(0x00000001)).isEqualTo(CommandStatus.ESME_RINVMSGLEN);
        assertThat(CommandStatus.fromCode(0x00000008)).isEqualTo(CommandStatus.ESME_RSYSERR);
        assertThat(CommandStatus.fromCode(0x0000000E)).isEqualTo(CommandStatus.ESME_RINVPASWD);
    }

    @Test
    @DisplayName("should return unknown error for invalid code")
    void shouldReturnUnknownErrorForInvalidCode() {
        assertThat(CommandStatus.fromCode(0x99999999)).isEqualTo(CommandStatus.ESME_RUNKNOWNERR);
    }

    @Test
    @DisplayName("should have correct codes for authentication errors")
    void shouldHaveCorrectCodesForAuthErrors() {
        assertThat(CommandStatus.ESME_RBINDFAIL.code()).isEqualTo(0x0000000D);
        assertThat(CommandStatus.ESME_RINVPASWD.code()).isEqualTo(0x0000000E);
        assertThat(CommandStatus.ESME_RINVSYSID.code()).isEqualTo(0x0000000F);
    }

    @Test
    @DisplayName("should have correct codes for address errors")
    void shouldHaveCorrectCodesForAddressErrors() {
        assertThat(CommandStatus.ESME_RINVSRCADR.code()).isEqualTo(0x0000000A);
        assertThat(CommandStatus.ESME_RINVDSTADR.code()).isEqualTo(0x0000000B);
        assertThat(CommandStatus.ESME_RINVSRCTON.code()).isEqualTo(0x00000048);
        assertThat(CommandStatus.ESME_RINVSRCNPI.code()).isEqualTo(0x00000049);
    }

    @Test
    @DisplayName("should have correct code for throttling error")
    void shouldHaveCorrectCodeForThrottling() {
        assertThat(CommandStatus.ESME_RTHROTTLED.code()).isEqualTo(0x00000058);
    }

    @Test
    @DisplayName("should have description for all statuses")
    void shouldHaveDescriptionForAllStatuses() {
        for (CommandStatus status : CommandStatus.values()) {
            assertThat(status.description()).isNotNull().isNotEmpty();
        }
    }

    @Test
    @DisplayName("should have meaningful descriptions")
    void shouldHaveMeaningfulDescriptions() {
        assertThat(CommandStatus.OK.description()).isEqualTo("No Error");
        assertThat(CommandStatus.ESME_RINVMSGLEN.description()).isEqualTo("Invalid Message Length");
        assertThat(CommandStatus.ESME_RSYSERR.description()).isEqualTo("System Error");
        assertThat(CommandStatus.ESME_RTHROTTLED.description()).isEqualTo("Throttling error");
    }

    @Test
    @DisplayName("should have correct codes for TLV errors")
    void shouldHaveCorrectCodesForTlvErrors() {
        assertThat(CommandStatus.ESME_RINVTLVSTREAM.code()).isEqualTo(0x000000C0);
        assertThat(CommandStatus.ESME_RTLVNOTALLWD.code()).isEqualTo(0x000000C1);
        assertThat(CommandStatus.ESME_RINVTLVLEN.code()).isEqualTo(0x000000C2);
        assertThat(CommandStatus.ESME_RMISSINGTLV.code()).isEqualTo(0x000000C3);
        assertThat(CommandStatus.ESME_RINVTLVVAL.code()).isEqualTo(0x000000C4);
    }

    @Test
    @DisplayName("should have correct code for unknown error")
    void shouldHaveCorrectCodeForUnknownError() {
        assertThat(CommandStatus.ESME_RUNKNOWNERR.code()).isEqualTo(0x000000FF);
    }
}
