package io.smppgateway.smpp.pdu;

import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GenericNack Tests")
class GenericNackTest {

    @Test
    @DisplayName("should create GenericNack with sequence number and status")
    void shouldCreateGenericNackWithSequenceNumberAndStatus() {
        GenericNack nack = new GenericNack(123, CommandStatus.ESME_RINVCMDID);

        assertThat(nack.sequenceNumber()).isEqualTo(123);
        assertThat(nack.commandStatus()).isEqualTo(CommandStatus.ESME_RINVCMDID);
    }

    @Test
    @DisplayName("should have correct command ID")
    void shouldHaveCorrectCommandId() {
        GenericNack nack = new GenericNack(1, CommandStatus.ESME_RSYSERR);

        assertThat(nack.commandId()).isEqualTo(CommandId.GENERIC_NACK);
    }

    @Test
    @DisplayName("should create invalid command ID nack")
    void shouldCreateInvalidCommandIdNack() {
        GenericNack nack = GenericNack.invalidCommandId(456);

        assertThat(nack.sequenceNumber()).isEqualTo(456);
        assertThat(nack.commandStatus()).isEqualTo(CommandStatus.ESME_RINVCMDID);
    }

    @Test
    @DisplayName("should create invalid command length nack")
    void shouldCreateInvalidCommandLengthNack() {
        GenericNack nack = GenericNack.invalidCommandLength(789);

        assertThat(nack.sequenceNumber()).isEqualTo(789);
        assertThat(nack.commandStatus()).isEqualTo(CommandStatus.ESME_RINVCMDLEN);
    }

    @Test
    @DisplayName("should create invalid message length nack")
    void shouldCreateInvalidMessageLengthNack() {
        GenericNack nack = GenericNack.invalidMessageLength(111);

        assertThat(nack.sequenceNumber()).isEqualTo(111);
        assertThat(nack.commandStatus()).isEqualTo(CommandStatus.ESME_RINVMSGLEN);
    }

    @Test
    @DisplayName("should create system error nack")
    void shouldCreateSystemErrorNack() {
        GenericNack nack = GenericNack.systemError(222);

        assertThat(nack.sequenceNumber()).isEqualTo(222);
        assertThat(nack.commandStatus()).isEqualTo(CommandStatus.ESME_RSYSERR);
    }

    @Test
    @DisplayName("should have empty optional parameters")
    void shouldHaveEmptyOptionalParameters() {
        GenericNack nack = new GenericNack(1, CommandStatus.ESME_RSYSERR);

        assertThat(nack.optionalParameters()).isEmpty();
    }

    @Test
    @DisplayName("should be a Pdu")
    void shouldBePdu() {
        GenericNack nack = new GenericNack(1, CommandStatus.ESME_RSYSERR);

        assertThat(nack).isInstanceOf(Pdu.class);
    }

    @Test
    @DisplayName("should work with zero sequence number")
    void shouldWorkWithZeroSequenceNumber() {
        // Zero sequence number is valid when the original PDU couldn't be decoded
        GenericNack nack = new GenericNack(0, CommandStatus.ESME_RSYSERR);

        assertThat(nack.sequenceNumber()).isZero();
    }
}
