package io.smppgateway.smpp.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppFrameDecoder Tests")
class SmppFrameDecoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new SmppFrameDecoder());
    }

    @Test
    @DisplayName("should have correct minimum PDU size constant")
    void shouldHaveCorrectMinPduSizeConstant() {
        assertThat(SmppFrameDecoder.MIN_PDU_SIZE).isEqualTo(16);
    }

    @Test
    @DisplayName("should have correct default max PDU size constant")
    void shouldHaveCorrectDefaultMaxPduSizeConstant() {
        assertThat(SmppFrameDecoder.DEFAULT_MAX_PDU_SIZE).isEqualTo(65536);
    }

    @Test
    @DisplayName("should decode valid PDU frame")
    void shouldDecodeValidPduFrame() {
        // Create a valid PDU header (16 bytes)
        ByteBuf input = Unpooled.buffer();
        input.writeInt(16);        // command_length
        input.writeInt(0x00000015); // command_id (enquire_link)
        input.writeInt(0x00000000); // command_status
        input.writeInt(1);          // sequence_number

        channel.writeInbound(input);

        ByteBuf output = channel.readInbound();
        assertThat(output).isNotNull();
        assertThat(output.readableBytes()).isEqualTo(16);

        output.release();
    }

    @Test
    @DisplayName("should decode PDU with body")
    void shouldDecodePduWithBody() {
        // Create a PDU with 24 bytes (16 header + 8 body)
        ByteBuf input = Unpooled.buffer();
        input.writeInt(24);         // command_length
        input.writeInt(0x00000004); // command_id (submit_sm)
        input.writeInt(0x00000000); // command_status
        input.writeInt(1);          // sequence_number
        input.writeBytes(new byte[8]); // body bytes

        channel.writeInbound(input);

        ByteBuf output = channel.readInbound();
        assertThat(output).isNotNull();
        assertThat(output.readableBytes()).isEqualTo(24);

        output.release();
    }

    @Test
    @DisplayName("should wait for complete frame before decoding")
    void shouldWaitForCompleteFrameBeforeDecoding() {
        // Send only partial data (less than command_length)
        ByteBuf partial = Unpooled.buffer();
        partial.writeInt(20);       // command_length = 20
        partial.writeInt(0x00000015); // command_id
        // Only 8 bytes sent, need 20 total

        channel.writeInbound(partial);

        // No output yet
        assertThat((ByteBuf) channel.readInbound()).isNull();

        // Send remaining bytes
        ByteBuf remaining = Unpooled.buffer();
        remaining.writeInt(0x00000000); // command_status
        remaining.writeInt(1);           // sequence_number
        remaining.writeBytes(new byte[4]); // body

        channel.writeInbound(remaining);

        ByteBuf output = channel.readInbound();
        assertThat(output).isNotNull();
        assertThat(output.readableBytes()).isEqualTo(20);

        output.release();
    }

    @Test
    @DisplayName("should reject PDU with command_length less than minimum")
    void shouldRejectPduWithCommandLengthLessThanMinimum() {
        ByteBuf input = Unpooled.buffer();
        input.writeInt(8);  // command_length < 16 (invalid)
        input.writeInt(0x00000015);
        input.writeInt(0);
        input.writeInt(1);

        assertThatThrownBy(() -> channel.writeInbound(input))
                .hasCauseInstanceOf(SmppDecodingException.class);
    }

    @Test
    @DisplayName("should decode multiple PDUs in sequence")
    void shouldDecodeMultiplePdusInSequence() {
        ByteBuf input = Unpooled.buffer();

        // First PDU
        input.writeInt(16);
        input.writeInt(0x00000015);
        input.writeInt(0);
        input.writeInt(1);

        // Second PDU
        input.writeInt(16);
        input.writeInt(0x80000015);
        input.writeInt(0);
        input.writeInt(1);

        channel.writeInbound(input);

        ByteBuf first = channel.readInbound();
        ByteBuf second = channel.readInbound();

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first.readableBytes()).isEqualTo(16);
        assertThat(second.readableBytes()).isEqualTo(16);

        first.release();
        second.release();
    }

    @Test
    @DisplayName("should create decoder with custom max PDU size")
    void shouldCreateDecoderWithCustomMaxPduSize() {
        EmbeddedChannel customChannel = new EmbeddedChannel(new SmppFrameDecoder(1024));

        ByteBuf input = Unpooled.buffer();
        input.writeInt(16);
        input.writeInt(0x00000015);
        input.writeInt(0);
        input.writeInt(1);

        customChannel.writeInbound(input);

        ByteBuf output = customChannel.readInbound();
        assertThat(output).isNotNull();

        output.release();
        customChannel.close();
    }

    @Test
    @DisplayName("should not decode when fewer than 4 bytes available")
    void shouldNotDecodeWhenFewerThan4BytesAvailable() {
        ByteBuf input = Unpooled.buffer();
        input.writeByte(0);
        input.writeByte(0);
        input.writeByte(0);

        channel.writeInbound(input);

        assertThat((ByteBuf) channel.readInbound()).isNull();
    }
}
