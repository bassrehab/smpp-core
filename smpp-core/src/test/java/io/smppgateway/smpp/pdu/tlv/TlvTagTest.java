package io.smppgateway.smpp.pdu.tlv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TlvTag Tests")
class TlvTagTest {

    @Test
    @DisplayName("should have correct code for destination address tags")
    void shouldHaveCorrectCodesForDestAddressTags() {
        assertThat(TlvTag.DEST_ADDR_SUBUNIT.code()).isEqualTo((short) 0x0005);
        assertThat(TlvTag.DEST_NETWORK_TYPE.code()).isEqualTo((short) 0x0006);
        assertThat(TlvTag.DEST_BEARER_TYPE.code()).isEqualTo((short) 0x0007);
        assertThat(TlvTag.DEST_TELEMATICS_ID.code()).isEqualTo((short) 0x0008);
    }

    @Test
    @DisplayName("should have correct code for source address tags")
    void shouldHaveCorrectCodesForSourceAddressTags() {
        assertThat(TlvTag.SOURCE_ADDR_SUBUNIT.code()).isEqualTo((short) 0x000D);
        assertThat(TlvTag.SOURCE_NETWORK_TYPE.code()).isEqualTo((short) 0x000E);
        assertThat(TlvTag.SOURCE_BEARER_TYPE.code()).isEqualTo((short) 0x000F);
        assertThat(TlvTag.SOURCE_TELEMATICS_ID.code()).isEqualTo((short) 0x0010);
    }

    @Test
    @DisplayName("should have correct code for message handling tags")
    void shouldHaveCorrectCodesForMessageHandlingTags() {
        assertThat(TlvTag.RECEIPTED_MESSAGE_ID.code()).isEqualTo((short) 0x001E);
        assertThat(TlvTag.MESSAGE_PAYLOAD.code()).isEqualTo((short) 0x0424);
        assertThat(TlvTag.MESSAGE_STATE.code()).isEqualTo((short) 0x0427);
    }

    @Test
    @DisplayName("should have correct code for SAR tags")
    void shouldHaveCorrectCodesForSarTags() {
        assertThat(TlvTag.SAR_MSG_REF_NUM.code()).isEqualTo((short) 0x020C);
        assertThat(TlvTag.SAR_TOTAL_SEGMENTS.code()).isEqualTo((short) 0x020E);
        assertThat(TlvTag.SAR_SEGMENT_SEQNUM.code()).isEqualTo((short) 0x020F);
    }

    @Test
    @DisplayName("should have correct code for callback tags")
    void shouldHaveCorrectCodesForCallbackTags() {
        assertThat(TlvTag.CALLBACK_NUM.code()).isEqualTo((short) 0x0381);
        assertThat(TlvTag.CALLBACK_NUM_PRES_IND.code()).isEqualTo((short) 0x0302);
        assertThat(TlvTag.CALLBACK_NUM_ATAG.code()).isEqualTo((short) 0x0303);
    }

    @Test
    @DisplayName("should have correct code for port tags")
    void shouldHaveCorrectCodesForPortTags() {
        assertThat(TlvTag.SOURCE_PORT.code()).isEqualTo((short) 0x020A);
        assertThat(TlvTag.DESTINATION_PORT.code()).isEqualTo((short) 0x020B);
    }

    @Test
    @DisplayName("should have correct code for delivery tags")
    void shouldHaveCorrectCodesForDeliveryTags() {
        assertThat(TlvTag.DELIVERY_FAILURE_REASON.code()).isEqualTo((short) 0x0425);
        assertThat(TlvTag.MORE_MESSAGES_TO_SEND.code()).isEqualTo((short) 0x0426);
        assertThat(TlvTag.NETWORK_ERROR_CODE.code()).isEqualTo((short) 0x0423);
    }

    @Test
    @DisplayName("should find tag by code")
    void shouldFindTagByCode() {
        assertThat(TlvTag.fromCode((short) 0x0424)).isEqualTo(TlvTag.MESSAGE_PAYLOAD);
        assertThat(TlvTag.fromCode((short) 0x001E)).isEqualTo(TlvTag.RECEIPTED_MESSAGE_ID);
        assertThat(TlvTag.fromCode((short) 0x020C)).isEqualTo(TlvTag.SAR_MSG_REF_NUM);
    }

    @Test
    @DisplayName("should return null for unknown code")
    void shouldReturnNullForUnknownCode() {
        assertThat(TlvTag.fromCode((short) 0x9999)).isNull();
        assertThat(TlvTag.fromCode((short) 0x7FFF)).isNull(); // Unknown code
    }

    @Test
    @DisplayName("should return VENDOR_SPECIFIC for code 0x0000")
    void shouldReturnVendorSpecificForCodeZero() {
        // VENDOR_SPECIFIC is a placeholder tag with code 0x0000
        assertThat(TlvTag.fromCode((short) 0x0000)).isEqualTo(TlvTag.VENDOR_SPECIFIC);
    }

    @Test
    @DisplayName("should have correct code for QoS tags")
    void shouldHaveCorrectCodesForQosTags() {
        assertThat(TlvTag.QOS_TIME_TO_LIVE.code()).isEqualTo((short) 0x0017);
        assertThat(TlvTag.PAYLOAD_TYPE.code()).isEqualTo((short) 0x0019);
    }

    @Test
    @DisplayName("should have correct code for USSD tag")
    void shouldHaveCorrectCodeForUssdTag() {
        assertThat(TlvTag.USSD_SERVICE_OP.code()).isEqualTo((short) 0x0501);
    }

    @Test
    @DisplayName("should have correct code for privacy tag")
    void shouldHaveCorrectCodeForPrivacyTag() {
        assertThat(TlvTag.PRIVACY_INDICATOR.code()).isEqualTo((short) 0x0201);
    }

    @Test
    @DisplayName("should have correct code for user reference tags")
    void shouldHaveCorrectCodesForUserReferenceTags() {
        assertThat(TlvTag.USER_MESSAGE_REFERENCE.code()).isEqualTo((short) 0x0204);
        assertThat(TlvTag.USER_RESPONSE_CODE.code()).isEqualTo((short) 0x0205);
    }

    @Test
    @DisplayName("should have correct code for SC interface version")
    void shouldHaveCorrectCodeForScInterfaceVersion() {
        assertThat(TlvTag.SC_INTERFACE_VERSION.code()).isEqualTo((short) 0x0210);
    }

    @Test
    @DisplayName("should have correct code for DPF tags")
    void shouldHaveCorrectCodesForDpfTags() {
        assertThat(TlvTag.DPF_RESULT.code()).isEqualTo((short) 0x0420);
        assertThat(TlvTag.SET_DPF.code()).isEqualTo((short) 0x0421);
    }

    @Test
    @DisplayName("should have correct code for broadcast tags")
    void shouldHaveCorrectCodesForBroadcastTags() {
        assertThat(TlvTag.BROADCAST_CHANNEL_INDICATOR.code()).isEqualTo((short) 0x0600);
        assertThat(TlvTag.BROADCAST_CONTENT_TYPE.code()).isEqualTo((short) 0x0601);
        assertThat(TlvTag.BROADCAST_MESSAGE_CLASS.code()).isEqualTo((short) 0x0603);
    }

    @Test
    @DisplayName("should have correct code for number portability tags")
    void shouldHaveCorrectCodesForNumberPortabilityTags() {
        assertThat(TlvTag.DEST_ADDR_NP_RESOLUTION.code()).isEqualTo((short) 0x0611);
        assertThat(TlvTag.DEST_ADDR_NP_INFORMATION.code()).isEqualTo((short) 0x0612);
        assertThat(TlvTag.DEST_ADDR_NP_COUNTRY.code()).isEqualTo((short) 0x0613);
    }

    @Test
    @DisplayName("should have all expected TLV tags")
    void shouldHaveAllExpectedTlvTags() {
        // There should be a significant number of standard tags
        assertThat(TlvTag.values().length).isGreaterThanOrEqualTo(50);
    }
}
