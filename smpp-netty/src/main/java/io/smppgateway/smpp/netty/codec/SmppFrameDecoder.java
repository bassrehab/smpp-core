package io.smppgateway.smpp.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SMPP frame decoder that extracts PDU frames based on the command_length field.
 *
 * <p>SMPP PDUs have a 4-byte length field at the start that includes the length
 * field itself. This decoder reads the length and extracts complete PDU frames.
 *
 * <p>PDU Header structure (16 bytes):
 * <pre>
 * command_length   : 4 bytes (total PDU size including this field)
 * command_id       : 4 bytes
 * command_status   : 4 bytes
 * sequence_number  : 4 bytes
 * </pre>
 */
public class SmppFrameDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger log = LoggerFactory.getLogger(SmppFrameDecoder.class);

    /**
     * Minimum PDU size (header only, no body).
     */
    public static final int MIN_PDU_SIZE = 16;

    /**
     * Default maximum PDU size (64KB).
     */
    public static final int DEFAULT_MAX_PDU_SIZE = 65536;

    /**
     * Creates a frame decoder with the default maximum PDU size.
     */
    public SmppFrameDecoder() {
        this(DEFAULT_MAX_PDU_SIZE);
    }

    /**
     * Creates a frame decoder with a custom maximum PDU size.
     *
     * @param maxPduSize Maximum allowed PDU size in bytes
     */
    public SmppFrameDecoder(int maxPduSize) {
        super(
            maxPduSize,    // maxFrameLength
            0,             // lengthFieldOffset (command_length is at position 0)
            4,             // lengthFieldLength (4 bytes)
            -4,            // lengthAdjustment (length includes itself)
            0              // initialBytesToStrip (keep the length field)
        );
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // Check if we have at least the minimum bytes to read the length
        if (in.readableBytes() < 4) {
            return null;
        }

        // Peek at the command_length without advancing reader index
        int commandLength = in.getInt(in.readerIndex());

        // Validate command length
        if (commandLength < MIN_PDU_SIZE) {
            log.warn("Invalid PDU: command_length {} is less than minimum {}",
                     commandLength, MIN_PDU_SIZE);
            throw new SmppDecodingException("Invalid command_length: " + commandLength);
        }

        // Let parent class handle the frame extraction
        return super.decode(ctx, in);
    }

    @Override
    protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        // Use slice to avoid memory copy (zero-copy)
        return buffer.retainedSlice(index, length);
    }
}
