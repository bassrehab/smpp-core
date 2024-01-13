package io.smppgateway.smpp.netty.window;

import io.smppgateway.smpp.pdu.PduRequest;
import io.smppgateway.smpp.pdu.PduResponse;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A future representing a pending SMPP request awaiting a response.
 *
 * <p>This wraps a CompletableFuture with additional metadata about the
 * request, including timing information for latency tracking.
 *
 * @param <R> The expected response type
 */
public class WindowFuture<R extends PduResponse> {

    private final PduRequest<R> request;
    private final int sequenceNumber;
    private final Instant createdAt;
    private final CompletableFuture<R> future;
    private volatile Instant completedAt;

    public WindowFuture(PduRequest<R> request, int sequenceNumber) {
        this.request = request;
        this.sequenceNumber = sequenceNumber;
        this.createdAt = Instant.now();
        this.future = new CompletableFuture<>();
    }

    /**
     * Returns the original request.
     */
    public PduRequest<R> getRequest() {
        return request;
    }

    /**
     * Returns the sequence number.
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Returns when this future was created.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns when this future was completed, or null if pending.
     */
    public Instant getCompletedAt() {
        return completedAt;
    }

    /**
     * Returns the underlying CompletableFuture.
     */
    public CompletableFuture<R> getFuture() {
        return future;
    }

    /**
     * Returns true if the request has received a response.
     */
    public boolean isDone() {
        return future.isDone();
    }

    /**
     * Returns true if the request was cancelled.
     */
    public boolean isCancelled() {
        return future.isCancelled();
    }

    /**
     * Completes the future with the response.
     */
    public void complete(R response) {
        this.completedAt = Instant.now();
        future.complete(response);
    }

    /**
     * Completes the future exceptionally.
     */
    public void completeExceptionally(Throwable cause) {
        this.completedAt = Instant.now();
        future.completeExceptionally(cause);
    }

    /**
     * Cancels the request.
     */
    public boolean cancel() {
        this.completedAt = Instant.now();
        return future.cancel(false);
    }

    /**
     * Waits for the response with a timeout.
     *
     * @param timeout The maximum time to wait
     * @param unit The time unit
     * @return The response
     * @throws TimeoutException If the timeout expires
     * @throws InterruptedException If the thread is interrupted
     */
    public R await(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        try {
            return future.get(timeout, unit);
        } catch (java.util.concurrent.ExecutionException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(e.getCause());
        }
    }

    /**
     * Returns the elapsed time in milliseconds, or -1 if not completed.
     */
    public long getElapsedMillis() {
        if (completedAt == null) {
            return -1;
        }
        return completedAt.toEpochMilli() - createdAt.toEpochMilli();
    }

    /**
     * Returns the elapsed time since creation in milliseconds.
     */
    public long getAgeMillis() {
        return Instant.now().toEpochMilli() - createdAt.toEpochMilli();
    }

    @Override
    public String toString() {
        return "WindowFuture{" +
               "seq=" + sequenceNumber +
               ", command=" + request.commandId() +
               ", done=" + isDone() +
               ", ageMs=" + getAgeMillis() +
               '}';
    }
}
