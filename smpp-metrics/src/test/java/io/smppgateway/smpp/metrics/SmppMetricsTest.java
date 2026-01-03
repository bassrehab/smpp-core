package io.smppgateway.smpp.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.smppgateway.smpp.pdu.SubmitSm;
import io.smppgateway.smpp.pdu.SubmitSmResp;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppMetrics Tests")
class SmppMetricsTest {

    private MeterRegistry registry;
    private SmppMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = SmppMetrics.create(registry, "smpp.test");
    }

    @Test
    @DisplayName("should create metrics with custom prefix")
    void shouldCreateMetricsWithCustomPrefix() {
        SmppMetrics m = SmppMetrics.create(registry, "custom.prefix");

        m.sessionCreated();

        assertThat(registry.find("custom.prefix.sessions.active").gauge()).isNotNull();
    }

    @Test
    @DisplayName("should create server metrics with server prefix")
    void shouldCreateServerMetricsWithServerPrefix() {
        SmppMetrics serverMetrics = SmppMetrics.forServer(registry);

        serverMetrics.sessionCreated();

        assertThat(registry.find("smpp.server.sessions.active").gauge()).isNotNull();
    }

    @Test
    @DisplayName("should create client metrics with client prefix")
    void shouldCreateClientMetricsWithClientPrefix() {
        SmppMetrics clientMetrics = SmppMetrics.forClient(registry);

        clientMetrics.sessionCreated();

        assertThat(registry.find("smpp.client.sessions.active").gauge()).isNotNull();
    }

    @Test
    @DisplayName("should track active sessions")
    void shouldTrackActiveSessions() {
        assertThat(metrics.getActiveSessions()).isZero();

        metrics.sessionCreated();
        assertThat(metrics.getActiveSessions()).isEqualTo(1);

        metrics.sessionCreated();
        assertThat(metrics.getActiveSessions()).isEqualTo(2);

        metrics.sessionDestroyed();
        assertThat(metrics.getActiveSessions()).isEqualTo(1);

        metrics.sessionDestroyed();
        assertThat(metrics.getActiveSessions()).isZero();
    }

    @Test
    @DisplayName("should track bound sessions")
    void shouldTrackBoundSessions() {
        assertThat(metrics.getBoundSessions()).isZero();

        metrics.sessionBound();
        assertThat(metrics.getBoundSessions()).isEqualTo(1);

        metrics.sessionBound();
        assertThat(metrics.getBoundSessions()).isEqualTo(2);

        metrics.sessionUnbound();
        assertThat(metrics.getBoundSessions()).isEqualTo(1);
    }

    @Test
    @DisplayName("should record PDU received by command type")
    void shouldRecordPduReceivedByCommandType() {
        metrics.recordPduReceived(CommandId.SUBMIT_SM);
        metrics.recordPduReceived(CommandId.SUBMIT_SM);
        metrics.recordPduReceived(CommandId.DELIVER_SM);

        assertThat(registry.find("smpp.test.pdu.received")
                .tag("command", "submit_sm")
                .counter().count()).isEqualTo(2.0);

        assertThat(registry.find("smpp.test.pdu.received")
                .tag("command", "deliver_sm")
                .counter().count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should record PDU received using PDU object")
    void shouldRecordPduReceivedUsingPduObject() {
        SubmitSm submitSm = SubmitSm.builder()
                .sequenceNumber(1)
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .build();

        metrics.recordPduReceived(submitSm);

        assertThat(registry.find("smpp.test.pdu.received")
                .tag("command", "submit_sm")
                .counter().count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should record PDU sent by command type")
    void shouldRecordPduSentByCommandType() {
        metrics.recordPduSent(CommandId.SUBMIT_SM);
        metrics.recordPduSent(CommandId.SUBMIT_SM_RESP);

        assertThat(registry.find("smpp.test.pdu.sent")
                .tag("command", "submit_sm")
                .counter().count()).isEqualTo(1.0);

        assertThat(registry.find("smpp.test.pdu.sent")
                .tag("command", "submit_sm_resp")
                .counter().count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should record request latency")
    void shouldRecordRequestLatency() {
        metrics.recordRequestLatency(CommandId.SUBMIT_SM, Duration.ofMillis(100));
        metrics.recordRequestLatency(CommandId.SUBMIT_SM, Duration.ofMillis(200));

        Timer timer = registry.find("smpp.test.request.duration")
                .tag("command", "submit_sm")
                .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS)).isBetween(100.0, 200.0);
    }

    @Test
    @DisplayName("should record request latency in milliseconds")
    void shouldRecordRequestLatencyInMilliseconds() {
        metrics.recordRequestLatencyMs(CommandId.DELIVER_SM, 50);

        Timer timer = registry.find("smpp.test.request.duration")
                .tag("command", "deliver_sm")
                .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("should use timer sample for latency measurement")
    void shouldUseTimerSampleForLatencyMeasurement() throws Exception {
        Timer.Sample sample = metrics.startTimer();
        Thread.sleep(50);
        metrics.stopTimer(sample, CommandId.ENQUIRE_LINK);

        Timer timer = registry.find("smpp.test.request.duration")
                .tag("command", "enquire_link")
                .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(50);
    }

    @Test
    @DisplayName("should record errors")
    void shouldRecordErrors() {
        metrics.recordError(CommandStatus.ESME_RTHROTTLED);
        metrics.recordError(CommandStatus.ESME_RTHROTTLED);
        metrics.recordError(CommandStatus.ESME_RINVPASWD);

        assertThat(registry.find("smpp.test.errors")
                .tag("status", "esme_rthrottled")
                .counter().count()).isEqualTo(2.0);

        assertThat(registry.find("smpp.test.errors")
                .tag("status", "esme_rinvpaswd")
                .counter().count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should not record ESME_ROK status as error")
    void shouldNotRecordEsmeRokStatusAsError() {
        // Library specifically checks for ESME_ROK (not OK) for the success check
        metrics.recordError(CommandStatus.ESME_ROK);

        assertThat(registry.find("smpp.test.errors").counters()).isEmpty();
    }

    @Test
    @DisplayName("should record window utilization")
    void shouldRecordWindowUtilization() {
        metrics.recordWindowUtilization(50, 100);

        assertThat(metrics.getPendingRequests()).isEqualTo(50);

        assertThat(registry.find("smpp.test.window.utilization")
                .summary()).isNotNull();
    }

    @Test
    @DisplayName("should handle zero max size in window utilization")
    void shouldHandleZeroMaxSizeInWindowUtilization() {
        // Should not throw
        metrics.recordWindowUtilization(0, 0);

        assertThat(metrics.getPendingRequests()).isZero();
    }

    @Test
    @DisplayName("should return meter registry")
    void shouldReturnMeterRegistry() {
        assertThat(metrics.getRegistry()).isEqualTo(registry);
    }

    @Test
    @DisplayName("should register gauges on creation")
    void shouldRegisterGaugesOnCreation() {
        assertThat(registry.find("smpp.test.sessions.active").gauge()).isNotNull();
        assertThat(registry.find("smpp.test.sessions.bound").gauge()).isNotNull();
        assertThat(registry.find("smpp.test.requests.pending").gauge()).isNotNull();
    }

    @Test
    @DisplayName("gauges should have descriptions")
    void gaugesShouldHaveDescriptions() {
        assertThat(registry.find("smpp.test.sessions.active").gauge().getId().getDescription())
                .isEqualTo("Number of active SMPP sessions");

        assertThat(registry.find("smpp.test.sessions.bound").gauge().getId().getDescription())
                .isEqualTo("Number of bound SMPP sessions");

        assertThat(registry.find("smpp.test.requests.pending").gauge().getId().getDescription())
                .isEqualTo("Number of pending requests in window");
    }
}
