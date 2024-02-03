package io.smppgateway.smpp.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.smppgateway.smpp.server.SmppServer;
import io.smppgateway.smpp.server.SmppServerSession;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Micrometer MeterBinder for SMPP servers.
 *
 * <p>Automatically registers metrics for an SMPP server when bound to a registry.
 * This is the recommended way to integrate SMPP metrics with Spring Boot or
 * other Micrometer-enabled frameworks.
 *
 * <p>Example with Spring Boot:
 * <pre>
 * &#64;Bean
 * public SmppMeterBinder smppMeterBinder(SmppServer server) {
 *     return new SmppMeterBinder(server);
 * }
 * </pre>
 *
 * <p>Example manual binding:
 * <pre>
 * SmppMeterBinder binder = new SmppMeterBinder(server);
 * binder.bindTo(registry);
 * </pre>
 */
public class SmppMeterBinder implements MeterBinder {

    private final Supplier<SmppServer> serverSupplier;
    private final List<Tag> tags;

    /**
     * Creates a meter binder for the given server.
     *
     * @param server The SMPP server to monitor
     */
    public SmppMeterBinder(SmppServer server) {
        this(() -> server, Collections.emptyList());
    }

    /**
     * Creates a meter binder for the given server with custom tags.
     *
     * @param server The SMPP server to monitor
     * @param tags Additional tags to apply to all metrics
     */
    public SmppMeterBinder(SmppServer server, List<Tag> tags) {
        this(() -> server, tags);
    }

    /**
     * Creates a meter binder with a server supplier.
     *
     * @param serverSupplier Supplier for the SMPP server (lazy initialization)
     * @param tags Additional tags to apply to all metrics
     */
    public SmppMeterBinder(Supplier<SmppServer> serverSupplier, List<Tag> tags) {
        this.serverSupplier = serverSupplier;
        this.tags = tags;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        // Session count
        Gauge.builder("smpp.server.sessions", serverSupplier,
                server -> server != null ? server.getSessionCount() : 0)
            .tags(tags)
            .description("Number of active SMPP sessions")
            .register(registry);

        // Bound sessions
        Gauge.builder("smpp.server.sessions.bound", serverSupplier,
                server -> {
                    if (server == null) return 0;
                    return server.getSessions().stream()
                        .filter(SmppServerSession::isBound)
                        .count();
                })
            .tags(tags)
            .description("Number of bound SMPP sessions")
            .register(registry);

        // Server running status
        Gauge.builder("smpp.server.running", serverSupplier,
                server -> server != null && server.isRunning() ? 1 : 0)
            .tags(tags)
            .description("Server running status (1=running, 0=stopped)")
            .register(registry);

        // Transmitter sessions
        Gauge.builder("smpp.server.sessions.transmitter", serverSupplier,
                server -> {
                    if (server == null) return 0;
                    return server.getSessions().stream()
                        .filter(s -> s.getBindType() != null && s.canTransmit() && !s.canReceive())
                        .count();
                })
            .tags(tags)
            .description("Number of transmitter sessions")
            .register(registry);

        // Receiver sessions
        Gauge.builder("smpp.server.sessions.receiver", serverSupplier,
                server -> {
                    if (server == null) return 0;
                    return server.getSessions().stream()
                        .filter(s -> s.getBindType() != null && s.canReceive() && !s.canTransmit())
                        .count();
                })
            .tags(tags)
            .description("Number of receiver sessions")
            .register(registry);

        // Transceiver sessions
        Gauge.builder("smpp.server.sessions.transceiver", serverSupplier,
                server -> {
                    if (server == null) return 0;
                    return server.getSessions().stream()
                        .filter(s -> s.getBindType() != null && s.canReceive() && s.canTransmit())
                        .count();
                })
            .tags(tags)
            .description("Number of transceiver sessions")
            .register(registry);

        // Total submit_sm received
        Gauge.builder("smpp.server.submit.total", serverSupplier,
                server -> {
                    if (server == null) return 0;
                    return server.getSessions().stream()
                        .mapToLong(SmppServerSession::getSubmitSmReceived)
                        .sum();
                })
            .tags(tags)
            .description("Total submit_sm messages received")
            .register(registry);

        // Total deliver_sm sent
        Gauge.builder("smpp.server.deliver.total", serverSupplier,
                server -> {
                    if (server == null) return 0;
                    return server.getSessions().stream()
                        .mapToLong(SmppServerSession::getDeliverSmSent)
                        .sum();
                })
            .tags(tags)
            .description("Total deliver_sm messages sent")
            .register(registry);

        // Total errors
        Gauge.builder("smpp.server.errors.total", serverSupplier,
                server -> {
                    if (server == null) return 0;
                    return server.getSessions().stream()
                        .mapToLong(SmppServerSession::getErrors)
                        .sum();
                })
            .tags(tags)
            .description("Total errors across all sessions")
            .register(registry);
    }
}
