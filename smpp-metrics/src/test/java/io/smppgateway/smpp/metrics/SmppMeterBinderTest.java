package io.smppgateway.smpp.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.smppgateway.smpp.server.SmppServer;
import io.smppgateway.smpp.server.SmppServerSession;
import io.smppgateway.smpp.types.SmppBindType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SmppMeterBinder Tests")
class SmppMeterBinderTest {

    private MeterRegistry registry;
    private SmppServer mockServer;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        mockServer = mock(SmppServer.class);
    }

    @Test
    @DisplayName("should register session count gauge")
    void shouldRegisterSessionCountGauge() {
        when(mockServer.getSessionCount()).thenReturn(5);

        SmppMeterBinder binder = new SmppMeterBinder(mockServer);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.sessions").gauge().value()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("should register server running gauge")
    void shouldRegisterServerRunningGauge() {
        when(mockServer.isRunning()).thenReturn(true);

        SmppMeterBinder binder = new SmppMeterBinder(mockServer);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.running").gauge().value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should register server running as 0 when stopped")
    void shouldRegisterServerRunningAsZeroWhenStopped() {
        when(mockServer.isRunning()).thenReturn(false);

        SmppMeterBinder binder = new SmppMeterBinder(mockServer);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.running").gauge().value()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should register bound sessions gauge")
    void shouldRegisterBoundSessionsGauge() {
        SmppServerSession boundSession = mock(SmppServerSession.class);
        SmppServerSession unboundSession = mock(SmppServerSession.class);
        when(boundSession.isBound()).thenReturn(true);
        when(unboundSession.isBound()).thenReturn(false);
        when(mockServer.getSessions()).thenReturn(List.of(boundSession, unboundSession));

        SmppMeterBinder binder = new SmppMeterBinder(mockServer);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.sessions.bound").gauge().value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should register transmitter sessions gauge")
    void shouldRegisterTransmitterSessionsGauge() {
        SmppServerSession txSession = mock(SmppServerSession.class);
        when(txSession.getBindType()).thenReturn(SmppBindType.TRANSMITTER);
        when(txSession.canTransmit()).thenReturn(true);
        when(txSession.canReceive()).thenReturn(false);
        when(mockServer.getSessions()).thenReturn(List.of(txSession));

        SmppMeterBinder binder = new SmppMeterBinder(mockServer);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.sessions.transmitter").gauge().value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should register receiver sessions gauge")
    void shouldRegisterReceiverSessionsGauge() {
        SmppServerSession rxSession = mock(SmppServerSession.class);
        when(rxSession.getBindType()).thenReturn(SmppBindType.RECEIVER);
        when(rxSession.canReceive()).thenReturn(true);
        when(rxSession.canTransmit()).thenReturn(false);
        when(mockServer.getSessions()).thenReturn(List.of(rxSession));

        SmppMeterBinder binder = new SmppMeterBinder(mockServer);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.sessions.receiver").gauge().value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should register transceiver sessions gauge")
    void shouldRegisterTransceiverSessionsGauge() {
        SmppServerSession trxSession = mock(SmppServerSession.class);
        when(trxSession.getBindType()).thenReturn(SmppBindType.TRANSCEIVER);
        when(trxSession.canReceive()).thenReturn(true);
        when(trxSession.canTransmit()).thenReturn(true);
        when(mockServer.getSessions()).thenReturn(List.of(trxSession));

        SmppMeterBinder binder = new SmppMeterBinder(mockServer);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.sessions.transceiver").gauge().value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should register submit total gauge")
    void shouldRegisterSubmitTotalGauge() {
        SmppServerSession session = mock(SmppServerSession.class);
        when(session.getSubmitSmReceived()).thenReturn(100L);
        when(mockServer.getSessions()).thenReturn(List.of(session));

        SmppMeterBinder binder = new SmppMeterBinder(mockServer);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.submit.total").gauge().value()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("should register deliver total gauge")
    void shouldRegisterDeliverTotalGauge() {
        SmppServerSession session = mock(SmppServerSession.class);
        when(session.getDeliverSmSent()).thenReturn(50L);
        when(mockServer.getSessions()).thenReturn(List.of(session));

        SmppMeterBinder binder = new SmppMeterBinder(mockServer);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.deliver.total").gauge().value()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("should register errors total gauge")
    void shouldRegisterErrorsTotalGauge() {
        SmppServerSession session = mock(SmppServerSession.class);
        when(session.getErrors()).thenReturn(10L);
        when(mockServer.getSessions()).thenReturn(List.of(session));

        SmppMeterBinder binder = new SmppMeterBinder(mockServer);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.errors.total").gauge().value()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("should sum statistics across sessions")
    void shouldSumStatisticsAcrossSessions() {
        SmppServerSession session1 = mock(SmppServerSession.class);
        SmppServerSession session2 = mock(SmppServerSession.class);
        when(session1.getSubmitSmReceived()).thenReturn(100L);
        when(session2.getSubmitSmReceived()).thenReturn(200L);
        when(mockServer.getSessions()).thenReturn(List.of(session1, session2));

        SmppMeterBinder binder = new SmppMeterBinder(mockServer);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.submit.total").gauge().value()).isEqualTo(300.0);
    }

    @Test
    @DisplayName("should apply custom tags")
    void shouldApplyCustomTags() {
        List<Tag> tags = List.of(Tag.of("environment", "test"));
        when(mockServer.getSessionCount()).thenReturn(1);

        SmppMeterBinder binder = new SmppMeterBinder(mockServer, tags);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.sessions")
                .tag("environment", "test")
                .gauge().value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should handle null server from supplier")
    void shouldHandleNullServerFromSupplier() {
        SmppMeterBinder binder = new SmppMeterBinder(() -> null, List.of());
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.sessions").gauge().value()).isEqualTo(0.0);
        assertThat(registry.get("smpp.server.running").gauge().value()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should use supplier for lazy initialization")
    void shouldUseSupplierForLazyInitialization() {
        SmppMeterBinder binder = new SmppMeterBinder(() -> mockServer, List.of());
        when(mockServer.getSessionCount()).thenReturn(3);

        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.sessions").gauge().value()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("should handle empty session list")
    void shouldHandleEmptySessionList() {
        when(mockServer.getSessions()).thenReturn(List.of());

        SmppMeterBinder binder = new SmppMeterBinder(mockServer);
        binder.bindTo(registry);

        assertThat(registry.get("smpp.server.sessions.bound").gauge().value()).isEqualTo(0.0);
        assertThat(registry.get("smpp.server.submit.total").gauge().value()).isEqualTo(0.0);
    }
}
