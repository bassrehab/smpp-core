package io.smppgateway.smpp.benchmark;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * JMH benchmarks for SMPP message processing throughput.
 *
 * <p>Measures the full processing path:
 * <ul>
 *   <li>Client encodes SubmitSm</li>
 *   <li>Server decodes SubmitSm</li>
 *   <li>Server creates and encodes SubmitSmResp</li>
 *   <li>Client decodes SubmitSmResp</li>
 * </ul>
 *
 * <p>This simulates the codec path without actual network I/O,
 * measuring pure processing throughput.
 *
 * <p>Run with: java -jar smpp-benchmarks.jar ThroughputBenchmark
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 3, time = 10)
public class ThroughputBenchmark {

    private SubmitSm submitSm;
    private ByteBuf encodedSubmitSm;
    private final AtomicLong messageIdCounter = new AtomicLong(0);
    private final AtomicLong sequenceCounter = new AtomicLong(1);

    @Setup(Level.Trial)
    public void setup() {
        // Pre-create a sample SubmitSm
        submitSm = SubmitSm.builder()
                .sequenceNumber(1)
                .serviceType("")
                .sourceAddress(new Address((byte) 1, (byte) 1, "SENDER"))
                .destAddress(new Address((byte) 1, (byte) 1, "+14155551234"))
                .esmClass(EsmClass.DEFAULT)
                .registeredDelivery(RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED)
                .dataCoding(DataCoding.DEFAULT)
                .shortMessage("Hello World! This is a benchmark SMS.".getBytes(StandardCharsets.ISO_8859_1))
                .build();

        // Pre-encode for decode-only tests
        EmbeddedChannel ch = new EmbeddedChannel(new SmppPduEncoder());
        ch.writeOutbound(submitSm);
        ByteBuf encoded = ch.readOutbound();
        encodedSubmitSm = Unpooled.copiedBuffer(encoded);
        encoded.release();
        ch.close();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (encodedSubmitSm != null) {
            encodedSubmitSm.release();
        }
    }

    /**
     * Simulates full message round-trip processing:
     * 1. Client creates and encodes SubmitSm
     * 2. Server decodes SubmitSm
     * 3. Server handler processes and creates response
     * 4. Server encodes SubmitSmResp
     * 5. Client decodes SubmitSmResp
     *
     * This measures the full codec + handler processing path.
     */
    @Benchmark
    public void fullMessageRoundTrip(Blackhole bh) {
        int seq = (int) sequenceCounter.getAndIncrement();

        // 1. Client creates SubmitSm with new sequence
        SubmitSm request = SubmitSm.builder()
                .sequenceNumber(seq)
                .serviceType("")
                .sourceAddress(new Address((byte) 1, (byte) 1, "SENDER"))
                .destAddress(new Address((byte) 1, (byte) 1, "+14155551234"))
                .esmClass(EsmClass.DEFAULT)
                .registeredDelivery(RegisteredDelivery.NONE)
                .dataCoding(DataCoding.DEFAULT)
                .shortMessage("Benchmark message".getBytes(StandardCharsets.ISO_8859_1))
                .build();

        // 2. Client encodes SubmitSm
        EmbeddedChannel clientEncoder = new EmbeddedChannel(new SmppPduEncoder());
        clientEncoder.writeOutbound(request);
        ByteBuf encodedRequest = clientEncoder.readOutbound();

        // 3. Server decodes SubmitSm
        EmbeddedChannel serverDecoder = new EmbeddedChannel(new SmppPduDecoder());
        serverDecoder.writeInbound(encodedRequest);
        SubmitSm decodedRequest = serverDecoder.readInbound();

        // 4. Server handler creates response
        String messageId = Long.toHexString(messageIdCounter.incrementAndGet());
        SubmitSmResp response = new SubmitSmResp(
                decodedRequest.sequenceNumber(),
                CommandStatus.ESME_ROK,
                messageId,
                List.of()
        );

        // 5. Server encodes SubmitSmResp
        EmbeddedChannel serverEncoder = new EmbeddedChannel(new SmppPduEncoder());
        serverEncoder.writeOutbound(response);
        ByteBuf encodedResponse = serverEncoder.readOutbound();

        // 6. Client decodes SubmitSmResp
        EmbeddedChannel clientDecoder = new EmbeddedChannel(new SmppPduDecoder());
        clientDecoder.writeInbound(encodedResponse);
        SubmitSmResp decodedResponse = clientDecoder.readInbound();

        bh.consume(decodedResponse);

        // Cleanup
        clientEncoder.close();
        serverDecoder.close();
        serverEncoder.close();
        clientDecoder.close();
    }

    /**
     * Measures just the server-side processing:
     * decode request -> create response -> encode response
     */
    @Benchmark
    public void serverSideProcessing(Blackhole bh) {
        // Decode incoming request
        ByteBuf requestCopy = encodedSubmitSm.retainedDuplicate();
        EmbeddedChannel decoder = new EmbeddedChannel(new SmppPduDecoder());
        decoder.writeInbound(requestCopy);
        SubmitSm request = decoder.readInbound();

        // Handler creates response
        String messageId = Long.toHexString(messageIdCounter.incrementAndGet());
        SubmitSmResp response = new SubmitSmResp(
                request.sequenceNumber(),
                CommandStatus.ESME_ROK,
                messageId,
                List.of()
        );

        // Encode response
        EmbeddedChannel encoder = new EmbeddedChannel(new SmppPduEncoder());
        encoder.writeOutbound(response);
        ByteBuf encodedResponse = encoder.readOutbound();

        bh.consume(encodedResponse);

        encodedResponse.release();
        decoder.close();
        encoder.close();
    }

    /**
     * Measures handler processing only (no codec overhead).
     * This shows pure business logic throughput.
     */
    @Benchmark
    public void handlerProcessingOnly(Blackhole bh) {
        // Simulate handler: receive request, generate response
        String messageId = Long.toHexString(messageIdCounter.incrementAndGet());
        SubmitSmResp response = submitSm.createResponse(CommandStatus.ESME_ROK, messageId);
        bh.consume(response);
    }

    /**
     * Measures DeliverSm (delivery receipt) processing path.
     */
    @Benchmark
    public void deliveryReceiptProcessing(Blackhole bh) {
        int seq = (int) sequenceCounter.getAndIncrement();

        // Create delivery receipt
        DeliverSm deliverSm = DeliverSm.builder()
                .sequenceNumber(seq)
                .serviceType("")
                .sourceAddress(new Address((byte) 1, (byte) 1, "+14155551234"))
                .destAddress(new Address((byte) 1, (byte) 1, "RECEIVER"))
                .esmClass(EsmClass.fromByte((byte) 0x04)) // Delivery receipt
                .dataCoding(DataCoding.DEFAULT)
                .shortMessage("id:1234 sub:001 dlvrd:001 submit date:2401010000 done date:2401010001 stat:DELIVRD".getBytes())
                .build();

        // Encode
        EmbeddedChannel encoder = new EmbeddedChannel(new SmppPduEncoder());
        encoder.writeOutbound(deliverSm);
        ByteBuf encoded = encoder.readOutbound();

        // Decode
        EmbeddedChannel decoder = new EmbeddedChannel(new SmppPduDecoder());
        decoder.writeInbound(encoded);
        DeliverSm decoded = decoder.readInbound();

        // Create response
        DeliverSmResp response = new DeliverSmResp(decoded.sequenceNumber(), CommandStatus.ESME_ROK, "", List.of());

        bh.consume(response);

        encoder.close();
        decoder.close();
    }
}
