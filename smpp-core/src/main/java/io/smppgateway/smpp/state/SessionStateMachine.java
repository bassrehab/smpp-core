package io.smppgateway.smpp.state;

import io.smppgateway.smpp.exception.SmppInvalidStateException;
import io.smppgateway.smpp.types.SmppBindType;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe state machine for SMPP session lifecycle.
 * <p>
 * State transitions:
 * <pre>
 * CLOSED -> (connect) -> OPEN
 * OPEN -> (bind_transmitter) -> BOUND_TX
 * OPEN -> (bind_receiver) -> BOUND_RX
 * OPEN -> (bind_transceiver) -> BOUND_TRX
 * BOUND_* -> (unbind) -> CLOSED
 * ANY -> (error/disconnect) -> CLOSED
 * </pre>
 */
public class SessionStateMachine {

    private final ReentrantLock lock = new ReentrantLock();
    private volatile SessionState state = SessionState.CLOSED;

    /**
     * Returns the current session state.
     */
    public SessionState state() {
        return state;
    }

    /**
     * Returns true if the session is bound.
     */
    public boolean isBound() {
        return state.isBound();
    }

    /**
     * Returns true if the session is open (connected but not bound).
     */
    public boolean isOpen() {
        return state == SessionState.OPEN;
    }

    /**
     * Returns true if the session is closed.
     */
    public boolean isClosed() {
        return state == SessionState.CLOSED;
    }

    /**
     * Transitions from CLOSED to OPEN (connection established).
     *
     * @throws SmppInvalidStateException if not in CLOSED state
     */
    public void onConnect() {
        lock.lock();
        try {
            if (state != SessionState.CLOSED) {
                throw new SmppInvalidStateException(
                        "Cannot connect: expected CLOSED, was " + state);
            }
            state = SessionState.OPEN;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Transitions from OPEN to bound state based on bind type.
     *
     * @param bindType the bind type
     * @throws SmppInvalidStateException if not in OPEN state
     */
    public void onBind(SmppBindType bindType) {
        lock.lock();
        try {
            if (state != SessionState.OPEN) {
                throw new SmppInvalidStateException(
                        "Cannot bind: expected OPEN, was " + state);
            }
            state = SessionState.fromBindType(bindType);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Transitions from any bound state to CLOSED (graceful unbind).
     *
     * @throws SmppInvalidStateException if not in a bound state
     */
    public void onUnbind() {
        lock.lock();
        try {
            if (!state.isBound()) {
                throw new SmppInvalidStateException(
                        "Cannot unbind: not bound, was " + state);
            }
            state = SessionState.CLOSED;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Transitions to CLOSED from any state (error or forced disconnect).
     */
    public void onClose() {
        lock.lock();
        try {
            state = SessionState.CLOSED;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Validates that the session can transmit messages.
     *
     * @throws SmppInvalidStateException if session cannot transmit
     */
    public void validateCanTransmit() {
        if (!state.canTransmit()) {
            throw new SmppInvalidStateException(
                    "Cannot transmit in state: " + state);
        }
    }

    /**
     * Validates that the session can receive messages.
     *
     * @throws SmppInvalidStateException if session cannot receive
     */
    public void validateCanReceive() {
        if (!state.canReceive()) {
            throw new SmppInvalidStateException(
                    "Cannot receive in state: " + state);
        }
    }

    @Override
    public String toString() {
        return "SessionStateMachine[state=" + state + "]";
    }
}
