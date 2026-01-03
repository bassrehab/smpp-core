package io.smppgateway.smpp.state;

import io.smppgateway.smpp.exception.SmppInvalidStateException;
import io.smppgateway.smpp.types.SmppBindType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SessionStateMachine Tests")
class SessionStateMachineTest {

    private SessionStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new SessionStateMachine();
    }

    @Test
    @DisplayName("should start in CLOSED state")
    void shouldStartInClosedState() {
        assertThat(stateMachine.state()).isEqualTo(SessionState.CLOSED);
        assertThat(stateMachine.getState()).isEqualTo(SessionState.CLOSED);
        assertThat(stateMachine.isClosed()).isTrue();
        assertThat(stateMachine.isOpen()).isFalse();
        assertThat(stateMachine.isBound()).isFalse();
    }

    @Test
    @DisplayName("should transition from CLOSED to OPEN on connect")
    void shouldTransitionToOpenOnConnect() {
        stateMachine.onConnect();

        assertThat(stateMachine.state()).isEqualTo(SessionState.OPEN);
        assertThat(stateMachine.isOpen()).isTrue();
        assertThat(stateMachine.isClosed()).isFalse();
    }

    @Test
    @DisplayName("should throw exception when connecting from non-CLOSED state")
    void shouldThrowExceptionWhenConnectingFromNonClosedState() {
        stateMachine.onConnect(); // Now in OPEN

        assertThatThrownBy(() -> stateMachine.onConnect())
                .isInstanceOf(SmppInvalidStateException.class)
                .hasMessageContaining("Cannot connect")
                .hasMessageContaining("expected CLOSED");
    }

    @Test
    @DisplayName("should transition from OPEN to BOUND_TX on bind transmitter")
    void shouldTransitionToBoundTxOnBindTransmitter() {
        stateMachine.onConnect();
        stateMachine.onBind(SmppBindType.TRANSMITTER);

        assertThat(stateMachine.state()).isEqualTo(SessionState.BOUND_TX);
        assertThat(stateMachine.isBound()).isTrue();
    }

    @Test
    @DisplayName("should transition from OPEN to BOUND_RX on bind receiver")
    void shouldTransitionToBoundRxOnBindReceiver() {
        stateMachine.onConnect();
        stateMachine.onBind(SmppBindType.RECEIVER);

        assertThat(stateMachine.state()).isEqualTo(SessionState.BOUND_RX);
        assertThat(stateMachine.isBound()).isTrue();
    }

    @Test
    @DisplayName("should transition from OPEN to BOUND_TRX on bind transceiver")
    void shouldTransitionToBoundTrxOnBindTransceiver() {
        stateMachine.onConnect();
        stateMachine.onBind(SmppBindType.TRANSCEIVER);

        assertThat(stateMachine.state()).isEqualTo(SessionState.BOUND_TRX);
        assertThat(stateMachine.isBound()).isTrue();
    }

    @Test
    @DisplayName("should throw exception when binding from non-OPEN state")
    void shouldThrowExceptionWhenBindingFromNonOpenState() {
        // CLOSED state
        assertThatThrownBy(() -> stateMachine.onBind(SmppBindType.TRANSCEIVER))
                .isInstanceOf(SmppInvalidStateException.class)
                .hasMessageContaining("Cannot bind")
                .hasMessageContaining("expected OPEN");
    }

    @Test
    @DisplayName("should throw exception when binding from already bound state")
    void shouldThrowExceptionWhenBindingFromBoundState() {
        stateMachine.onConnect();
        stateMachine.onBind(SmppBindType.TRANSCEIVER);

        assertThatThrownBy(() -> stateMachine.onBind(SmppBindType.TRANSMITTER))
                .isInstanceOf(SmppInvalidStateException.class)
                .hasMessageContaining("Cannot bind");
    }

    @Test
    @DisplayName("should transition from bound state to CLOSED on unbind")
    void shouldTransitionToClosedOnUnbind() {
        stateMachine.onConnect();
        stateMachine.onBind(SmppBindType.TRANSCEIVER);
        stateMachine.onUnbind();

        assertThat(stateMachine.state()).isEqualTo(SessionState.CLOSED);
        assertThat(stateMachine.isClosed()).isTrue();
    }

    @Test
    @DisplayName("should throw exception when unbinding from non-bound state")
    void shouldThrowExceptionWhenUnbindingFromNonBoundState() {
        // CLOSED state
        assertThatThrownBy(() -> stateMachine.onUnbind())
                .isInstanceOf(SmppInvalidStateException.class)
                .hasMessageContaining("Cannot unbind")
                .hasMessageContaining("not bound");
    }

    @Test
    @DisplayName("should throw exception when unbinding from OPEN state")
    void shouldThrowExceptionWhenUnbindingFromOpenState() {
        stateMachine.onConnect();

        assertThatThrownBy(() -> stateMachine.onUnbind())
                .isInstanceOf(SmppInvalidStateException.class)
                .hasMessageContaining("Cannot unbind");
    }

    @Test
    @DisplayName("should transition to CLOSED from any state on close")
    void shouldTransitionToClosedOnClose() {
        stateMachine.onConnect();
        stateMachine.onBind(SmppBindType.TRANSCEIVER);

        stateMachine.onClose();

        assertThat(stateMachine.state()).isEqualTo(SessionState.CLOSED);
    }

    @Test
    @DisplayName("should allow close from CLOSED state without exception")
    void shouldAllowCloseFromClosedState() {
        assertThatNoException().isThrownBy(() -> stateMachine.onClose());
        assertThat(stateMachine.state()).isEqualTo(SessionState.CLOSED);
    }

    @Test
    @DisplayName("should allow close from OPEN state")
    void shouldAllowCloseFromOpenState() {
        stateMachine.onConnect();

        assertThatNoException().isThrownBy(() -> stateMachine.onClose());
        assertThat(stateMachine.state()).isEqualTo(SessionState.CLOSED);
    }

    @ParameterizedTest
    @EnumSource(SmppBindType.class)
    @DisplayName("should allow close from any bound state")
    void shouldAllowCloseFromAnyBoundState(SmppBindType bindType) {
        stateMachine.onConnect();
        stateMachine.onBind(bindType);

        assertThatNoException().isThrownBy(() -> stateMachine.onClose());
        assertThat(stateMachine.state()).isEqualTo(SessionState.CLOSED);
    }

    @Test
    @DisplayName("should validate can transmit in TX state")
    void shouldValidateCanTransmitInTxState() {
        stateMachine.onConnect();
        stateMachine.onBind(SmppBindType.TRANSMITTER);

        assertThatNoException().isThrownBy(() -> stateMachine.validateCanTransmit());
    }

    @Test
    @DisplayName("should validate can transmit in TRX state")
    void shouldValidateCanTransmitInTrxState() {
        stateMachine.onConnect();
        stateMachine.onBind(SmppBindType.TRANSCEIVER);

        assertThatNoException().isThrownBy(() -> stateMachine.validateCanTransmit());
    }

    @Test
    @DisplayName("should throw exception when validating transmit in RX state")
    void shouldThrowExceptionWhenValidatingTransmitInRxState() {
        stateMachine.onConnect();
        stateMachine.onBind(SmppBindType.RECEIVER);

        assertThatThrownBy(() -> stateMachine.validateCanTransmit())
                .isInstanceOf(SmppInvalidStateException.class)
                .hasMessageContaining("Cannot transmit");
    }

    @Test
    @DisplayName("should validate can receive in RX state")
    void shouldValidateCanReceiveInRxState() {
        stateMachine.onConnect();
        stateMachine.onBind(SmppBindType.RECEIVER);

        assertThatNoException().isThrownBy(() -> stateMachine.validateCanReceive());
    }

    @Test
    @DisplayName("should validate can receive in TRX state")
    void shouldValidateCanReceiveInTrxState() {
        stateMachine.onConnect();
        stateMachine.onBind(SmppBindType.TRANSCEIVER);

        assertThatNoException().isThrownBy(() -> stateMachine.validateCanReceive());
    }

    @Test
    @DisplayName("should throw exception when validating receive in TX state")
    void shouldThrowExceptionWhenValidatingReceiveInTxState() {
        stateMachine.onConnect();
        stateMachine.onBind(SmppBindType.TRANSMITTER);

        assertThatThrownBy(() -> stateMachine.validateCanReceive())
                .isInstanceOf(SmppInvalidStateException.class)
                .hasMessageContaining("Cannot receive");
    }

    @Test
    @DisplayName("should throw exception when validating transmit in CLOSED state")
    void shouldThrowExceptionWhenValidatingTransmitInClosedState() {
        assertThatThrownBy(() -> stateMachine.validateCanTransmit())
                .isInstanceOf(SmppInvalidStateException.class);
    }

    @Test
    @DisplayName("should throw exception when validating receive in CLOSED state")
    void shouldThrowExceptionWhenValidatingReceiveInClosedState() {
        assertThatThrownBy(() -> stateMachine.validateCanReceive())
                .isInstanceOf(SmppInvalidStateException.class);
    }

    @Test
    @DisplayName("should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        String result = stateMachine.toString();

        assertThat(result).contains("SessionStateMachine");
        assertThat(result).contains("CLOSED");
    }

    @Test
    @DisplayName("should be thread-safe during concurrent state changes")
    void shouldBeThreadSafeDuringConcurrentStateChanges() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successfulConnects = new AtomicInteger(0);
        AtomicInteger failedConnects = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    stateMachine.onConnect();
                    successfulConnects.incrementAndGet();
                } catch (SmppInvalidStateException e) {
                    failedConnects.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Only one connect should succeed, all others should fail
        assertThat(successfulConnects.get()).isEqualTo(1);
        assertThat(failedConnects.get()).isEqualTo(threadCount - 1);
        assertThat(stateMachine.state()).isEqualTo(SessionState.OPEN);
    }

    @Test
    @DisplayName("should handle full lifecycle correctly")
    void shouldHandleFullLifecycleCorrectly() {
        // Start closed
        assertThat(stateMachine.isClosed()).isTrue();

        // Connect
        stateMachine.onConnect();
        assertThat(stateMachine.isOpen()).isTrue();

        // Bind
        stateMachine.onBind(SmppBindType.TRANSCEIVER);
        assertThat(stateMachine.isBound()).isTrue();

        // Unbind
        stateMachine.onUnbind();
        assertThat(stateMachine.isClosed()).isTrue();

        // Can connect again
        stateMachine.onConnect();
        assertThat(stateMachine.isOpen()).isTrue();
    }
}
