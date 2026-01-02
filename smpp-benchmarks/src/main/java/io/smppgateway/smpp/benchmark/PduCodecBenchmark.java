package io.smppgateway.smpp.benchmark;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.smppgateway.smpp.netty.codec.SmppPduDecoder;
import io.smppgateway.smpp.netty.codec.SmppPduEncoder;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.types.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for SMPP PDU encoding and decoding performance.
 *
 * <p>Measures throughput of:
 * <ul>
 *   <li>SubmitSm encoding/decoding</li>
 *   <li>DeliverSm encoding/decoding</li>
 *   <li>BindTransceiver encoding/decoding</li>
 *   <li>EnquireLink encoding/decoding (minimal PDU)</li>
 * </ul>
 *
 * <p>Run with: java -jar smpp-benchmarks.jar PduCodecBenchmark
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 10)
public class PduCodecBenchmark {

    private EmbeddedChannel encoderChannel;
    private EmbeddedChannel decoderChannel;

    private SubmitSm submitSm;
    private DeliverSm deliverSm;
    private BindTransceiver bindTransceiver;
    private EnquireLink enquireLink;

    // Pre-encoded PDU bytes for decode benchmarks
    private ByteBuf submitSmBytes;
    private ByteBuf deliverSmBytes;
    private ByteBuf bindTransceiverBytes;
    private ByteBuf enquireLinkBytes;

    @Setup(Level.Trial)
    public void setup() {
        // Create channels with encoder/decoder
        encoderChannel = new EmbeddedChannel(new SmppPduEncoder());
        decoderChannel = new EmbeddedChannel(new SmppPduDecoder());

        // Create sample PDUs
        submitSm = SubmitSm.builder()
                .sequenceNumber(1)
                .serviceType("")
                .sourceAddress(new Address((byte) 1, (byte) 1, "SENDER"))
                .destAddress(new Address((byte) 1, (byte) 1, "+14155551234"))
                .esmClass(EsmClass.DEFAULT)
                .registeredDelivery(RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED)
                .dataCoding(DataCoding.DEFAULT)
                .shortMessage("Hello World! This is a test SMS message.".getBytes(StandardCharsets.ISO_8859_1))
                .build();

        deliverSm = DeliverSm.builder()
                .sequenceNumber(1)
                .serviceType("")
                .sourceAddress(new Address((byte) 1, (byte) 1, "+14155551234"))
                .destAddress(new Address((byte) 1, (byte) 1, "RECEIVER"))
                .esmClass(EsmClass.DEFAULT)
                .dataCoding(DataCoding.DEFAULT)
                .shortMessage("Incoming message content for testing.".getBytes(StandardCharsets.ISO_8859_1))
                .build();

        bindTransceiver = new BindTransceiver(
                1,
                "test-system",
                "password123",
                "SMPP",
                (byte) 0x34,
                (byte) 1,
                (byte) 1,
                "",
                List.of()
        );

        enquireLink = new EnquireLink(1);

        // Pre-encode PDUs for decode benchmarks
        submitSmBytes = encodePdu(submitSm);
        deliverSmBytes = encodePdu(deliverSm);
        bindTransceiverBytes = encodePdu(bindTransceiver);
        enquireLinkBytes = encodePdu(enquireLink);
    }

    private ByteBuf encodePdu(Pdu pdu) {
        EmbeddedChannel ch = new EmbeddedChannel(new SmppPduEncoder());
        ch.writeOutbound(pdu);
        ByteBuf encoded = ch.readOutbound();
        // Copy to unpooled buffer for reuse
        ByteBuf copy = Unpooled.copiedBuffer(encoded);
        encoded.release();
        ch.close();
        return copy;
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (encoderChannel != null) {
            encoderChannel.close();
        }
        if (decoderChannel != null) {
            decoderChannel.close();
        }
        if (submitSmBytes != null) {
            submitSmBytes.release();
        }
        if (deliverSmBytes != null) {
            deliverSmBytes.release();
        }
        if (bindTransceiverBytes != null) {
            bindTransceiverBytes.release();
        }
        if (enquireLinkBytes != null) {
            enquireLinkBytes.release();
        }
    }

    // =========== Encoding Benchmarks ===========

    @Benchmark
    public void encodeSubmitSm(Blackhole bh) {
        EmbeddedChannel ch = new EmbeddedChannel(new SmppPduEncoder());
        ch.writeOutbound(submitSm);
        ByteBuf encoded = ch.readOutbound();
        bh.consume(encoded);
        encoded.release();
        ch.close();
    }

    @Benchmark
    public void encodeDeliverSm(Blackhole bh) {
        EmbeddedChannel ch = new EmbeddedChannel(new SmppPduEncoder());
        ch.writeOutbound(deliverSm);
        ByteBuf encoded = ch.readOutbound();
        bh.consume(encoded);
        encoded.release();
        ch.close();
    }

    @Benchmark
    public void encodeBindTransceiver(Blackhole bh) {
        EmbeddedChannel ch = new EmbeddedChannel(new SmppPduEncoder());
        ch.writeOutbound(bindTransceiver);
        ByteBuf encoded = ch.readOutbound();
        bh.consume(encoded);
        encoded.release();
        ch.close();
    }

    @Benchmark
    public void encodeEnquireLink(Blackhole bh) {
        EmbeddedChannel ch = new EmbeddedChannel(new SmppPduEncoder());
        ch.writeOutbound(enquireLink);
        ByteBuf encoded = ch.readOutbound();
        bh.consume(encoded);
        encoded.release();
        ch.close();
    }

    // =========== Decoding Benchmarks ===========

    @Benchmark
    public void decodeSubmitSm(Blackhole bh) {
        ByteBuf copy = submitSmBytes.retainedDuplicate();
        EmbeddedChannel ch = new EmbeddedChannel(new SmppPduDecoder());
        ch.writeInbound(copy);
        Pdu decoded = ch.readInbound();
        bh.consume(decoded);
        ch.close();
    }

    @Benchmark
    public void decodeDeliverSm(Blackhole bh) {
        ByteBuf copy = deliverSmBytes.retainedDuplicate();
        EmbeddedChannel ch = new EmbeddedChannel(new SmppPduDecoder());
        ch.writeInbound(copy);
        Pdu decoded = ch.readInbound();
        bh.consume(decoded);
        ch.close();
    }

    @Benchmark
    public void decodeBindTransceiver(Blackhole bh) {
        ByteBuf copy = bindTransceiverBytes.retainedDuplicate();
        EmbeddedChannel ch = new EmbeddedChannel(new SmppPduDecoder());
        ch.writeInbound(copy);
        Pdu decoded = ch.readInbound();
        bh.consume(decoded);
        ch.close();
    }

    @Benchmark
    public void decodeEnquireLink(Blackhole bh) {
        ByteBuf copy = enquireLinkBytes.retainedDuplicate();
        EmbeddedChannel ch = new EmbeddedChannel(new SmppPduDecoder());
        ch.writeInbound(copy);
        Pdu decoded = ch.readInbound();
        bh.consume(decoded);
        ch.close();
    }

    // =========== Round-trip Benchmark ===========

    @Benchmark
    public void roundTripSubmitSm(Blackhole bh) {
        // Encode
        EmbeddedChannel encCh = new EmbeddedChannel(new SmppPduEncoder());
        encCh.writeOutbound(submitSm);
        ByteBuf encoded = encCh.readOutbound();

        // Decode
        EmbeddedChannel decCh = new EmbeddedChannel(new SmppPduDecoder());
        decCh.writeInbound(encoded);
        Pdu decoded = decCh.readInbound();
        bh.consume(decoded);

        encCh.close();
        decCh.close();
    }

    @Benchmark
    public void roundTripDeliverSm(Blackhole bh) {
        // Encode
        EmbeddedChannel encCh = new EmbeddedChannel(new SmppPduEncoder());
        encCh.writeOutbound(deliverSm);
        ByteBuf encoded = encCh.readOutbound();

        // Decode
        EmbeddedChannel decCh = new EmbeddedChannel(new SmppPduDecoder());
        decCh.writeInbound(encoded);
        Pdu decoded = decCh.readInbound();
        bh.consume(decoded);

        encCh.close();
        decCh.close();
    }
}
