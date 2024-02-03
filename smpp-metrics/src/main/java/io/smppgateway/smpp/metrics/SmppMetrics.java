package io.smppgateway.smpp.metrics;

import io.micrometer.core.instrument.*;
import io.smppgateway.smpp.pdu.Pdu;
import io.smppgateway.smpp.types.CommandId;
import io.smppgateway.smpp.types.CommandStatus;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SMPP metrics collector using Micrometer.
 *
 * <p>Provides comprehensive metrics for SMPP sessions including:
 * <ul>
 *   <li>Session counts (active, bound)</li>
 *   <li>PDU counters (by type and direction)</li>
 *   <li>Request latency histograms</li>
 *   <li>Error counters</li>
 *   <li>Window utilization</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * MeterRegistry registry = new SimpleMeterRegistry();
 * SmppMetrics metrics = SmppMetrics.create(registry, "smpp.server");
 *
 * // Record PDU
 * metrics.recordPduReceived(submitSm);
 *
 * // Record request latency
 * metrics.recordRequestLatency(CommandId.SUBMIT_SM, Duration.ofMillis(50));
 * </pre>
 */
public class SmppMetrics {

    private final MeterRegistry registry;
    private final String prefix;

    // Gauges
    private final AtomicInteger activeSessions = new AtomicInteger(0);
    private final AtomicInteger boundSessions = new AtomicInteger(0);
    private final AtomicInteger pendingRequests = new AtomicInteger(0);

    // Counters by command
    private final ConcurrentHashMap<CommandId, Counter> pduReceivedCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<CommandId, Counter> pduSentCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<CommandStatus, Counter> errorCounters = new ConcurrentHashMap<>();

    // Timers by command
    private final ConcurrentHashMap<CommandId, Timer> requestTimers = new ConcurrentHashMap<>();

    // Summary for window utilization
    private final DistributionSummary windowUtilization;

    private SmppMetrics(MeterRegistry registry, String prefix) {
        this.registry = registry;
        this.prefix = prefix;

        // Register gauges
        Gauge.builder(prefix + ".sessions.active", activeSessions, AtomicInteger::get)
            .description("Number of active SMPP sessions")
            .register(registry);

        Gauge.builder(prefix + ".sessions.bound", boundSessions, AtomicInteger::get)
            .description("Number of bound SMPP sessions")
            .register(registry);

        Gauge.builder(prefix + ".requests.pending", pendingRequests, AtomicInteger::get)
            .description("Number of pending requests in window")
            .register(registry);

        // Register window utilization summary
        windowUtilization = DistributionSummary.builder(prefix + ".window.utilization")
            .description("Window utilization percentage")
            .baseUnit("percent")
            .register(registry);
    }

    /**
     * Creates a new SmppMetrics instance.
     *
     * @param registry The Micrometer registry
     * @param prefix The metric name prefix (e.g., "smpp.server" or "smpp.client")
     */
    public static SmppMetrics create(MeterRegistry registry, String prefix) {
        return new SmppMetrics(registry, prefix);
    }

    /**
     * Creates metrics for an SMPP server.
     */
    public static SmppMetrics forServer(MeterRegistry registry) {
        return create(registry, "smpp.server");
    }

    /**
     * Creates metrics for an SMPP client.
     */
    public static SmppMetrics forClient(MeterRegistry registry) {
        return create(registry, "smpp.client");
    }

    // Session lifecycle

    /**
     * Records a new session being created.
     */
    public void sessionCreated() {
        activeSessions.incrementAndGet();
    }

    /**
     * Records a session being destroyed.
     */
    public void sessionDestroyed() {
        activeSessions.decrementAndGet();
    }

    /**
     * Records a session becoming bound.
     */
    public void sessionBound() {
        boundSessions.incrementAndGet();
    }

    /**
     * Records a session becoming unbound.
     */
    public void sessionUnbound() {
        boundSessions.decrementAndGet();
    }

    // PDU counts

    /**
     * Records a PDU being received.
     */
    public void recordPduReceived(Pdu pdu) {
        recordPduReceived(pdu.commandId());
    }

    /**
     * Records a PDU being received by command type.
     */
    public void recordPduReceived(CommandId commandId) {
        pduReceivedCounters.computeIfAbsent(commandId, id ->
            Counter.builder(prefix + ".pdu.received")
                .tag("command", id.name().toLowerCase())
                .description("Number of PDUs received")
                .register(registry)
        ).increment();
    }

    /**
     * Records a PDU being sent.
     */
    public void recordPduSent(Pdu pdu) {
        recordPduSent(pdu.commandId());
    }

    /**
     * Records a PDU being sent by command type.
     */
    public void recordPduSent(CommandId commandId) {
        pduSentCounters.computeIfAbsent(commandId, id ->
            Counter.builder(prefix + ".pdu.sent")
                .tag("command", id.name().toLowerCase())
                .description("Number of PDUs sent")
                .register(registry)
        ).increment();
    }

    // Request latency

    /**
     * Records request/response latency.
     */
    public void recordRequestLatency(CommandId commandId, Duration latency) {
        requestTimers.computeIfAbsent(commandId, id ->
            Timer.builder(prefix + ".request.duration")
                .tag("command", id.name().toLowerCase())
                .description("Request latency")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry)
        ).record(latency);
    }

    /**
     * Records request/response latency in milliseconds.
     */
    public void recordRequestLatencyMs(CommandId commandId, long millis) {
        recordRequestLatency(commandId, Duration.ofMillis(millis));
    }

    /**
     * Starts timing a request and returns a sample to stop later.
     */
    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    /**
     * Stops a timer sample and records the latency.
     */
    public void stopTimer(Timer.Sample sample, CommandId commandId) {
        Timer timer = requestTimers.computeIfAbsent(commandId, id ->
            Timer.builder(prefix + ".request.duration")
                .tag("command", id.name().toLowerCase())
                .description("Request latency")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry)
        );
        sample.stop(timer);
    }

    // Errors

    /**
     * Records an error response.
     */
    public void recordError(CommandStatus status) {
        if (status != CommandStatus.ESME_ROK) {
            errorCounters.computeIfAbsent(status, s ->
                Counter.builder(prefix + ".errors")
                    .tag("status", s.name().toLowerCase())
                    .tag("code", String.valueOf(s.code()))
                    .description("Number of errors")
                    .register(registry)
            ).increment();
        }
    }

    // Window utilization

    /**
     * Updates window utilization.
     *
     * @param pending Number of pending requests
     * @param maxSize Maximum window size
     */
    public void recordWindowUtilization(int pending, int maxSize) {
        pendingRequests.set(pending);
        if (maxSize > 0) {
            double utilization = (pending * 100.0) / maxSize;
            windowUtilization.record(utilization);
        }
    }

    // Accessors

    /**
     * Returns the number of active sessions.
     */
    public int getActiveSessions() {
        return activeSessions.get();
    }

    /**
     * Returns the number of bound sessions.
     */
    public int getBoundSessions() {
        return boundSessions.get();
    }

    /**
     * Returns the number of pending requests.
     */
    public int getPendingRequests() {
        return pendingRequests.get();
    }

    /**
     * Returns the meter registry.
     */
    public MeterRegistry getRegistry() {
        return registry;
    }
}
