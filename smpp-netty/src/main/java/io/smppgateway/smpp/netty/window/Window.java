package io.smppgateway.smpp.netty.window;

import io.smppgateway.smpp.exception.SmppException;
import io.smppgateway.smpp.pdu.PduRequest;
import io.smppgateway.smpp.pdu.PduResponse;
import io.smppgateway.smpp.types.CommandStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Thread-safe sliding window for managing outstanding SMPP requests.
 *
 * <p>The window limits the number of concurrent unacknowledged requests
 * and provides correlation between requests and responses via sequence numbers.
 *
 * <p>Features:
 * <ul>
 *   <li>Configurable window size</li>
 *   <li>Automatic sequence number generation</li>
 *   <li>Request/response correlation</li>
 *   <li>Timeout handling</li>
 *   <li>Metrics (pending count, offer failures)</li>
 * </ul>
 *
 * @param <R> The response type
 */
public class Window<R extends PduResponse> {

    private static final Logger log = LoggerFactory.getLogger(Window.class);

    private final int maxSize;
    private final Semaphore permits;
    private final ConcurrentHashMap<Integer, WindowFuture<R>> pending;
    private final Duration requestTimeout;

    private volatile boolean closed = false;

    /**
     * Creates a window with the specified size.
     *
     * @param maxSize Maximum number of concurrent pending requests
     * @param requestTimeout Default timeout for requests
     */
    public Window(int maxSize, Duration requestTimeout) {
        this.maxSize = maxSize;
        this.permits = new Semaphore(maxSize);
        this.pending = new ConcurrentHashMap<>();
        this.requestTimeout = requestTimeout;
    }

    /**
     * Creates a window with default 30 second timeout.
     */
    public Window(int maxSize) {
        this(maxSize, Duration.ofSeconds(30));
    }

    /**
     * Offers a request to the window, waiting up to the specified timeout
     * for a slot to become available.
     *
     * <p>The request's sequence number is used for correlation with the response.
     * Ensure the request has a unique sequence number set before calling this method.
     *
     * @param request The request to send (must have sequence number set)
     * @param timeout Maximum time to wait for a slot
     * @return A future that will complete when the response arrives
     * @throws TimeoutException If no slot becomes available in time
     * @throws InterruptedException If the thread is interrupted
     */
    @SuppressWarnings("unchecked")
    public <T extends PduResponse> WindowFuture<T> offer(PduRequest<T> request, Duration timeout)
            throws TimeoutException, InterruptedException {

        if (closed) {
            throw new SmppException(CommandStatus.ESME_RINVBNDSTS, "Window is closed");
        }

        // Wait for a permit (slot in the window)
        if (!permits.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            throw new TimeoutException("Timeout waiting for window slot");
        }

        try {
            // Use the PDU's sequence number for correlation
            int seq = request.sequenceNumber();
            WindowFuture<T> future = new WindowFuture<>(request, seq);

            // Store in pending map (we know the cast is safe because we control the types)
            pending.put(seq, (WindowFuture<R>) future);

            log.trace("Offered request to window: seq={} size={}", seq, pending.size());
            return future;
        } catch (Exception e) {
            permits.release();
            throw e;
        }
    }

    /**
     * Offers a request without blocking.
     *
     * <p>The request's sequence number is used for correlation with the response.
     * Ensure the request has a unique sequence number set before calling this method.
     *
     * @param request The request to send (must have sequence number set)
     * @return The future, or null if the window is full
     */
    @SuppressWarnings("unchecked")
    public <T extends PduResponse> WindowFuture<T> tryOffer(PduRequest<T> request) {
        if (closed) {
            return null;
        }

        if (!permits.tryAcquire()) {
            return null;
        }

        try {
            // Use the PDU's sequence number for correlation
            int seq = request.sequenceNumber();
            WindowFuture<T> future = new WindowFuture<>(request, seq);
            pending.put(seq, (WindowFuture<R>) future);
            log.trace("Offered request to window: seq={} size={}", seq, pending.size());
            return future;
        } catch (Exception e) {
            permits.release();
            throw e;
        }
    }

    /**
     * Completes a pending request with its response.
     *
     * @param sequenceNumber The sequence number from the response
     * @param response The response PDU
     * @return true if the request was found and completed
     */
    @SuppressWarnings("unchecked")
    public boolean complete(int sequenceNumber, R response) {
        WindowFuture<R> future = pending.remove(sequenceNumber);
        if (future != null) {
            permits.release();
            future.complete(response);
            log.trace("Completed request: seq={} size={}", sequenceNumber, pending.size());
            return true;
        }
        log.warn("No pending request for sequence number: {}", sequenceNumber);
        return false;
    }

    /**
     * Fails a pending request with an exception.
     *
     * @param sequenceNumber The sequence number
     * @param cause The failure reason
     * @return true if the request was found and failed
     */
    public boolean fail(int sequenceNumber, Throwable cause) {
        WindowFuture<R> future = pending.remove(sequenceNumber);
        if (future != null) {
            permits.release();
            future.completeExceptionally(cause);
            log.trace("Failed request: seq={} reason={}", sequenceNumber, cause.getMessage());
            return true;
        }
        return false;
    }

    /**
     * Cancels a pending request.
     *
     * @param sequenceNumber The sequence number
     * @return true if the request was found and cancelled
     */
    public boolean cancel(int sequenceNumber) {
        WindowFuture<R> future = pending.remove(sequenceNumber);
        if (future != null) {
            permits.release();
            future.cancel();
            log.trace("Cancelled request: seq={}", sequenceNumber);
            return true;
        }
        return false;
    }

    /**
     * Expires all requests older than the request timeout.
     *
     * @return The number of expired requests
     */
    public int expireOldRequests() {
        return expireOldRequests(requestTimeout);
    }

    /**
     * Expires all requests older than the specified age.
     *
     * @param maxAge Maximum age before expiration
     * @return The number of expired requests
     */
    public int expireOldRequests(Duration maxAge) {
        long maxAgeMs = maxAge.toMillis();
        List<Integer> expired = new ArrayList<>();

        for (var entry : pending.entrySet()) {
            if (entry.getValue().getAgeMillis() > maxAgeMs) {
                expired.add(entry.getKey());
            }
        }

        for (int seq : expired) {
            fail(seq, new TimeoutException("Request timeout"));
        }

        if (!expired.isEmpty()) {
            log.debug("Expired {} old requests", expired.size());
        }

        return expired.size();
    }

    /**
     * Cancels all pending requests.
     *
     * @param cause The reason for cancellation
     * @return The number of cancelled requests
     */
    public int cancelAll(Throwable cause) {
        List<Integer> sequences = new ArrayList<>(pending.keySet());
        for (int seq : sequences) {
            fail(seq, cause);
        }
        return sequences.size();
    }

    /**
     * Closes the window and cancels all pending requests.
     */
    public void close() {
        closed = true;
        cancelAll(new SmppException(CommandStatus.ESME_RINVBNDSTS, "Window closed"));
    }

    /**
     * Returns true if the window is closed.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Returns the number of pending requests.
     */
    public int size() {
        return pending.size();
    }

    /**
     * Returns true if the window is empty.
     */
    public boolean isEmpty() {
        return pending.isEmpty();
    }

    /**
     * Returns true if the window is full.
     */
    public boolean isFull() {
        return permits.availablePermits() == 0;
    }

    /**
     * Returns the maximum window size.
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Returns the number of available slots.
     */
    public int getAvailableSlots() {
        return permits.availablePermits();
    }

    /**
     * Returns all pending futures (for testing/debugging).
     */
    public Collection<WindowFuture<R>> getPendingFutures() {
        return new ArrayList<>(pending.values());
    }
}
