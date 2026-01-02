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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JMH benchmarks for SMPP network throughput with actual server/client.
 *
 * <p>This benchmark uses real network I/O to measure end-to-end throughput.
 *
 * <p>Run with: java -jar smpp-benchmarks.jar NetworkThroughputBenchmark
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 3, time = 10)
public class NetworkThroughputBenchmark {

    private static final int PORT = 12775;
    private static final String SYSTEM_ID = "benchmark";
    private static final String PASSWORD = "password";

    private SmppServer server;
    private SmppClient client;
    private SmppClientSession session;
    private final AtomicLong messageIdCounter = new AtomicLong(0);

    private SubmitSm submitSm;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        // Create server
        server = SmppServer.builder()
                .port(PORT)
                .systemId("smsc")
                .maxConnections(100)
                .windowSize(512)
                .bindTimeout(Duration.ofSeconds(30))
                .requestTimeout(Duration.ofSeconds(30))
                .enquireLinkInterval(Duration.ofMinutes(5))
                .handler(new BenchmarkServerHandler())
                .build();

        server.startSync();
        System.out.println("Server started on port " + PORT);

        // Create client
        client = SmppClient.builder()
                .host("localhost")
                .port(PORT)
                .systemId(SYSTEM_ID)
                .password(PASSWORD)
                .bindType(SmppBindType.TRANSCEIVER)
                .windowSize(512)
                .connectTimeout(Duration.ofSeconds(10))
                .bindTimeout(Duration.ofSeconds(30))
                .requestTimeout(Duration.ofSeconds(30))
                .enquireLinkInterval(Duration.ofMinutes(5))
                .noAutoReconnect()
                .handler(new BenchmarkClientHandler())
                .build();

        session = client.connect();
        System.out.println("Client connected and bound: " + session.isBound());

        // Pre-create a sample SubmitSm
        submitSm = SubmitSm.builder()
                .serviceType("")
                .sourceAddress(new Address((byte) 1, (byte) 1, "SENDER"))
                .destAddress(new Address((byte) 1, (byte) 1, "+14155551234"))
                .esmClass(EsmClass.DEFAULT)
                .registeredDelivery(RegisteredDelivery.NONE)
                .dataCoding(DataCoding.DEFAULT)
                .shortMessage("Benchmark message".getBytes())
                .build();

        // Allow connections to settle
        Thread.sleep(500);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        System.out.println("Tearing down...");
        if (client != null) {
            try {
                client.disconnect();
            } catch (Exception e) {
                // Ignore
            }
        }

        if (server != null) {
            try {
                server.stopSync();
            } catch (Exception e) {
                // Ignore
            }
        }
        System.out.println("Teardown complete");
    }

    /**
     * Benchmark synchronous submit_sm with actual network I/O.
     */
    @Benchmark
    public void submitSmSync(Blackhole bh) throws Exception {
        SubmitSmResp resp = session.submitSm(submitSm, Duration.ofSeconds(10));
        bh.consume(resp);
    }

    private class BenchmarkServerHandler implements SmppServerHandler {

        @Override
        public BindResult authenticate(SmppServerSession session, String systemId,
                                        String password, PduRequest<?> bindRequest) {
            if (SYSTEM_ID.equals(systemId) && PASSWORD.equals(password)) {
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

    private static class BenchmarkClientHandler implements SmppClientHandler {

        @Override
        public DeliverSmResult handleDeliverSm(SmppClientSession session, DeliverSm deliverSm) {
            return DeliverSmResult.success();
        }
    }
}
