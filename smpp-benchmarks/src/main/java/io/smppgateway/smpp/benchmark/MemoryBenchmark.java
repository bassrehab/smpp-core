package io.smppgateway.smpp.benchmark;

import io.smppgateway.smpp.client.SmppClient;
import io.smppgateway.smpp.client.SmppClientHandler;
import io.smppgateway.smpp.client.SmppClientSession;
import io.smppgateway.smpp.pdu.*;
import io.smppgateway.smpp.server.SmppServer;
import io.smppgateway.smpp.server.SmppServerHandler;
import io.smppgateway.smpp.server.SmppServerSession;
import io.smppgateway.smpp.types.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JMH benchmarks for memory usage and allocation analysis.
 *
 * <p>Measures:
 * <ul>
 *   <li>Memory per connection (virtual vs platform threads)</li>
 *   <li>PDU object allocation overhead</li>
 *   <li>Session memory footprint</li>
 * </ul>
 *
 * <p>Run with GC profiler for detailed allocation info:
 * {@code java -jar smpp-benchmarks.jar MemoryBenchmark -prof gc}
 *
 * <p>Run with JFR profiler:
 * {@code java -jar smpp-benchmarks.jar MemoryBenchmark -prof jfr}
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms4G", "-Xmx4G", "--enable-preview"})
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 3, time = 10)
public class MemoryBenchmark {

    private static final int BASE_PORT = 13775;
    private static final String SYSTEM_ID = "mem-bench";
    private static final String PASSWORD = "password";

    @Param({"10", "50", "100"})
    private int connectionCount;

    @Param({"true", "false"})
    private boolean useVirtualThreads;

    private SmppServer server;
    private List<SmppClient> clients;
    private List<SmppClientSession> sessions;
    private final AtomicLong messageIdCounter = new AtomicLong(0);

    @Setup(Level.Trial)
    public void setup() throws Exception {
        // Create server with configured thread model
        server = SmppServer.builder()
                .port(BASE_PORT)
                .systemId("smsc-memory")
                .maxConnections(connectionCount + 10)
                .windowSize(256)
                .bindTimeout(Duration.ofSeconds(30))
                .requestTimeout(Duration.ofSeconds(30))
                .enquireLinkInterval(Duration.ofMinutes(5))
                .handler(new MemoryServerHandler())
                .useVirtualThreads(useVirtualThreads)
                .build();

        server.startSync();

        // Create multiple clients
        clients = new ArrayList<>(connectionCount);
        sessions = new ArrayList<>(connectionCount);

        for (int i = 0; i < connectionCount; i++) {
            SmppClient client = SmppClient.builder()
                    .host("localhost")
                    .port(BASE_PORT)
                    .systemId(SYSTEM_ID + i)
                    .password(PASSWORD)
                    .bindType(SmppBindType.TRANSCEIVER)
                    .windowSize(256)
                    .connectTimeout(Duration.ofSeconds(10))
                    .bindTimeout(Duration.ofSeconds(30))
                    .requestTimeout(Duration.ofSeconds(30))
                    .enquireLinkInterval(Duration.ofMinutes(5))
                    .noAutoReconnect()
                    .handler(new MemoryClientHandler())
                    .build();

            clients.add(client);
            sessions.add(client.connect());
        }

        // Allow connections to settle
        Thread.sleep(1000);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        for (SmppClient client : clients) {
            try {
                client.disconnect();
            } catch (Exception e) {
                // Ignore
            }
        }
        clients.clear();
        sessions.clear();

        if (server != null) {
            try {
                server.stopSync();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Benchmark sending messages across all connections.
     * This measures the memory overhead per active connection under load.
     */
    @Benchmark
    public void sendAcrossAllConnections(Blackhole bh) throws Exception {
        SubmitSm submitSm = SubmitSm.builder()
                .serviceType("")
                .sourceAddress(new Address((byte) 1, (byte) 1, "SENDER"))
                .destAddress(new Address((byte) 1, (byte) 1, "+14155551234"))
                .esmClass(EsmClass.DEFAULT)
                .dataCoding(DataCoding.DEFAULT)
                .shortMessage("Memory benchmark test message.".getBytes())
                .build();

        // Send one message per connection
        for (SmppClientSession session : sessions) {
            if (session.isBound()) {
                SubmitSmResp resp = session.submitSm(submitSm, Duration.ofSeconds(10));
                bh.consume(resp);
            }
        }
    }

    /**
     * Benchmark PDU object creation overhead.
     */
    @Benchmark
    @Fork(jvmArgs = {"-Xms2G", "-Xmx2G"}) // Override for this specific test
    public void pduObjectAllocation(Blackhole bh) {
        // Create PDU objects to measure allocation overhead
        SubmitSm submitSm = SubmitSm.builder()
                .sequenceNumber(1)
                .serviceType("CMT")
                .sourceAddress(new Address((byte) 1, (byte) 1, "SENDER12345"))
                .destAddress(new Address((byte) 1, (byte) 1, "+14155551234"))
                .esmClass(EsmClass.DEFAULT)
                .protocolId((byte) 0)
                .priorityFlag((byte) 0)
                .registeredDelivery(RegisteredDelivery.SMSC_DELIVERY_RECEIPT_REQUESTED)
                .dataCoding(DataCoding.DEFAULT)
                .shortMessage("Test message for allocation benchmark.".getBytes())
                .build();
        bh.consume(submitSm);

        SubmitSmResp submitSmResp = new SubmitSmResp(1, CommandStatus.ESME_ROK, "ABC123", List.of());
        bh.consume(submitSmResp);

        DeliverSm deliverSm = DeliverSm.builder()
                .sequenceNumber(2)
                .sourceAddress(new Address((byte) 1, (byte) 1, "+14155551234"))
                .destAddress(new Address((byte) 1, (byte) 1, "RECEIVER"))
                .shortMessage("Delivery receipt content here.".getBytes())
                .build();
        bh.consume(deliverSm);

        EnquireLink enquireLink = new EnquireLink(3);
        bh.consume(enquireLink);

        EnquireLinkResp enquireLinkResp = new EnquireLinkResp(3, CommandStatus.ESME_ROK);
        bh.consume(enquireLinkResp);
    }

    private class MemoryServerHandler implements SmppServerHandler {

        @Override
        public BindResult authenticate(SmppServerSession session, String systemId,
                                       String password, PduRequest<?> bindRequest) {
            if (systemId.startsWith(SYSTEM_ID) && PASSWORD.equals(password)) {
                return BindResult.success();
            }
            return BindResult.failure(CommandStatus.ESME_RINVPASWD);
        }

        @Override
        public SubmitSmResult handleSubmitSm(SmppServerSession session, SubmitSm submitSm) {
            String messageId = Long.toHexString(messageIdCounter.incrementAndGet());
            return SubmitSmResult.success(messageId);
        }
    }

    private static class MemoryClientHandler implements SmppClientHandler {

        @Override
        public DeliverSmResult handleDeliverSm(SmppClientSession session, DeliverSm deliverSm) {
            return DeliverSmResult.success();
        }
    }
}
