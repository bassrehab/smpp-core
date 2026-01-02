package io.smppgateway.smpp.netty.window;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.smppgateway.smpp.pdu.Pdu;
import io.smppgateway.smpp.pdu.PduRequest;
import io.smppgateway.smpp.pdu.PduResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Netty handler that integrates the windowing system with the channel pipeline.
 *
 * <p>This handler:
 * <ul>
 *   <li>Intercepts outgoing requests and tracks them in the window</li>
 *   <li>Matches incoming responses to pending requests</li>
 *   <li>Periodically expires timed-out requests</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * WindowHandler handler = new WindowHandler(512, Duration.ofSeconds(30), executor);
 * pipeline.addLast("window", handler);
 *
 * // Send request and wait for response
 * WindowFuture<SubmitSmResp> future = handler.sendRequest(submitSm, Duration.ofSeconds(10));
 * SubmitSmResp response = future.await(10, TimeUnit.SECONDS);
 * }</pre>
 */
public class WindowHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(WindowHandler.class);

    private final Window<PduResponse> window;
    private final ScheduledExecutorService scheduler;
    private final Duration expirationCheckInterval;

    private ChannelHandlerContext ctx;
    private ScheduledFuture<?> expirationTask;

    /**
     * Creates a window handler.
     *
     * @param windowSize Maximum concurrent pending requests
     * @param requestTimeout Default request timeout
     * @param scheduler Scheduler for expiration checks
     */
    public WindowHandler(int windowSize, Duration requestTimeout, ScheduledExecutorService scheduler) {
        this.window = new Window<>(windowSize, requestTimeout);
        this.scheduler = scheduler;
        this.expirationCheckInterval = Duration.ofMillis(requestTimeout.toMillis() / 4);
    }

    /**
     * Creates a window handler with default 30 second timeout.
     */
    public WindowHandler(int windowSize, ScheduledExecutorService scheduler) {
        this(windowSize, Duration.ofSeconds(30), scheduler);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;

        // Start expiration task
        if (scheduler != null) {
            expirationTask = scheduler.scheduleAtFixedRate(
                this::checkExpiredRequests,
                expirationCheckInterval.toMillis(),
                expirationCheckInterval.toMillis(),
                TimeUnit.MILLISECONDS
            );
        }

        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (expirationTask != null) {
            expirationTask.cancel(false);
        }
        window.close();
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        window.close();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof PduResponse response) {
            // Try to match with pending request
            if (window.complete(response.sequenceNumber(), response)) {
                log.trace("Matched response: {} seq={}",
                         response.commandId(), response.sequenceNumber());
                return; // Response consumed by window
            }
        }

        // Pass through to next handler
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        window.close();
        super.exceptionCaught(ctx, cause);
    }

    /**
     * Sends a request and returns a future for the response.
     *
     * @param request The request to send
     * @param timeout Maximum time to wait for a window slot
     * @return Future that completes with the response
     * @throws TimeoutException If window is full and no slot becomes available
     * @throws InterruptedException If interrupted while waiting
     */
    public <R extends PduResponse> WindowFuture<R> sendRequest(PduRequest<R> request, Duration timeout)
            throws TimeoutException, InterruptedException {

        // Get a window slot
        WindowFuture<R> future = window.offer(request, timeout);

        // Create a new PDU with the assigned sequence number
        // Note: This assumes request records have a way to create copies with new sequence numbers
        // For now, we'll use the window's sequence number tracking

        // Write the request to the channel
        ChannelFuture writeFuture = ctx.writeAndFlush(request);

        // If write fails, cancel the window slot
        writeFuture.addListener(f -> {
            if (!f.isSuccess()) {
                window.fail(future.getSequenceNumber(), f.cause());
            }
        });

        return future;
    }

    /**
     * Tries to send a request without blocking if window is full.
     *
     * @param request The request to send
     * @return Future, or null if window is full
     */
    public <R extends PduResponse> WindowFuture<R> trySendRequest(PduRequest<R> request) {
        WindowFuture<R> future = window.tryOffer(request);
        if (future == null) {
            return null;
        }

        ChannelFuture writeFuture = ctx.writeAndFlush(request);
        writeFuture.addListener(f -> {
            if (!f.isSuccess()) {
                window.fail(future.getSequenceNumber(), f.cause());
            }
        });

        return future;
    }

    /**
     * Returns the number of pending requests.
     */
    public int getPendingCount() {
        return window.size();
    }

    /**
     * Returns the number of available window slots.
     */
    public int getAvailableSlots() {
        return window.getAvailableSlots();
    }

    /**
     * Returns true if the window is full.
     */
    public boolean isFull() {
        return window.isFull();
    }

    /**
     * Returns the underlying window (for testing).
     */
    public Window<PduResponse> getWindow() {
        return window;
    }

    private void checkExpiredRequests() {
        try {
            int expired = window.expireOldRequests();
            if (expired > 0) {
                log.debug("Expired {} requests in window check", expired);
            }
        } catch (Exception e) {
            log.error("Error in expiration check", e);
        }
    }
}
