package io.smppgateway.smpp.netty.window;

import io.smppgateway.smpp.pdu.SubmitSm;
import io.smppgateway.smpp.pdu.SubmitSmResp;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Window Tests")
class WindowTest {

    private Window<SubmitSmResp> window;

    @BeforeEach
    void setUp() {
        window = new Window<>(10, Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("should create window with specified size and timeout")
    void shouldCreateWindowWithSpecifiedSizeAndTimeout() {
        Window<SubmitSmResp> w = new Window<>(100, Duration.ofSeconds(30));

        assertThat(w.getMaxSize()).isEqualTo(100);
        assertThat(w.isEmpty()).isTrue();
        assertThat(w.size()).isZero();
        assertThat(w.isClosed()).isFalse();
    }

    @Test
    @DisplayName("should create window with default timeout")
    void shouldCreateWindowWithDefaultTimeout() {
        Window<SubmitSmResp> w = new Window<>(10);

        assertThat(w.getMaxSize()).isEqualTo(10);
        assertThat(w.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("should offer request and track in pending")
    void shouldOfferRequestAndTrackInPending() throws Exception {
        SubmitSm request = createSubmitSm(1);

        WindowFuture<SubmitSmResp> future = window.offer(request, Duration.ofSeconds(1));

        assertThat(future).isNotNull();
        assertThat(future.getSequenceNumber()).isEqualTo(1);
        assertThat(window.size()).isEqualTo(1);
        assertThat(window.getAvailableSlots()).isEqualTo(9);
    }

    @Test
    @DisplayName("should tryOffer request successfully when not full")
    void shouldTryOfferSuccessfullyWhenNotFull() {
        SubmitSm request = createSubmitSm(1);

        WindowFuture<SubmitSmResp> future = window.tryOffer(request);

        assertThat(future).isNotNull();
        assertThat(window.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("should return null from tryOffer when window is full")
    void shouldReturnNullFromTryOfferWhenFull() {
        // Fill the window
        for (int i = 0; i < 10; i++) {
            window.tryOffer(createSubmitSm(i));
        }

        assertThat(window.isFull()).isTrue();

        WindowFuture<SubmitSmResp> future = window.tryOffer(createSubmitSm(100));

        assertThat(future).isNull();
    }

    @Test
    @DisplayName("should throw TimeoutException when offer times out on full window")
    void shouldThrowTimeoutExceptionWhenOfferTimesOut() {
        // Fill the window
        for (int i = 0; i < 10; i++) {
            window.tryOffer(createSubmitSm(i));
        }

        assertThatThrownBy(() -> window.offer(createSubmitSm(100), Duration.ofMillis(50)))
                .isInstanceOf(TimeoutException.class)
                .hasMessageContaining("Timeout waiting for window slot");
    }

    @Test
    @DisplayName("should complete pending request with response")
    void shouldCompletePendingRequestWithResponse() throws Exception {
        SubmitSm request = createSubmitSm(42);
        WindowFuture<SubmitSmResp> future = window.offer(request, Duration.ofSeconds(1));

        SubmitSmResp response = new SubmitSmResp(42, CommandStatus.OK, "MSG123", null);
        boolean completed = window.complete(42, response);

        assertThat(completed).isTrue();
        assertThat(future.isDone()).isTrue();
        assertThat(future.getFuture().get()).isEqualTo(response);
        assertThat(window.size()).isZero();
        assertThat(window.getAvailableSlots()).isEqualTo(10);
    }

    @Test
    @DisplayName("should return false when completing unknown sequence number")
    void shouldReturnFalseWhenCompletingUnknownSequence() {
        boolean completed = window.complete(999, new SubmitSmResp(999, CommandStatus.OK, null, null));

        assertThat(completed).isFalse();
    }

    @Test
    @DisplayName("should fail pending request with exception")
    void shouldFailPendingRequestWithException() throws Exception {
        SubmitSm request = createSubmitSm(42);
        WindowFuture<SubmitSmResp> future = window.offer(request, Duration.ofSeconds(1));

        RuntimeException error = new RuntimeException("Connection lost");
        boolean failed = window.fail(42, error);

        assertThat(failed).isTrue();
        assertThat(future.isDone()).isTrue();
        assertThat(window.size()).isZero();

        assertThatThrownBy(() -> future.getFuture().get())
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("should cancel pending request")
    void shouldCancelPendingRequest() throws Exception {
        SubmitSm request = createSubmitSm(42);
        WindowFuture<SubmitSmResp> future = window.offer(request, Duration.ofSeconds(1));

        boolean cancelled = window.cancel(42);

        assertThat(cancelled).isTrue();
        assertThat(future.isCancelled()).isTrue();
        assertThat(window.size()).isZero();
    }

    @Test
    @DisplayName("should expire old requests")
    void shouldExpireOldRequests() throws Exception {
        Window<SubmitSmResp> shortTimeoutWindow = new Window<>(10, Duration.ofMillis(50));

        WindowFuture<SubmitSmResp> future = shortTimeoutWindow.offer(createSubmitSm(1), Duration.ofSeconds(1));

        // Wait for request to become old
        Thread.sleep(100);

        int expired = shortTimeoutWindow.expireOldRequests();

        assertThat(expired).isEqualTo(1);
        assertThat(future.isDone()).isTrue();
        assertThat(shortTimeoutWindow.size()).isZero();
    }

    @Test
    @DisplayName("should expire requests older than specified age")
    void shouldExpireRequestsOlderThanSpecifiedAge() throws Exception {
        WindowFuture<SubmitSmResp> future1 = window.offer(createSubmitSm(1), Duration.ofSeconds(1));
        Thread.sleep(100);
        WindowFuture<SubmitSmResp> future2 = window.offer(createSubmitSm(2), Duration.ofSeconds(1));

        int expired = window.expireOldRequests(Duration.ofMillis(50));

        // First request should be expired, second should remain
        assertThat(expired).isEqualTo(1);
        assertThat(future1.isDone()).isTrue();
        assertThat(window.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("should cancel all pending requests")
    void shouldCancelAllPendingRequests() throws Exception {
        WindowFuture<SubmitSmResp> future1 = window.offer(createSubmitSm(1), Duration.ofSeconds(1));
        WindowFuture<SubmitSmResp> future2 = window.offer(createSubmitSm(2), Duration.ofSeconds(1));
        WindowFuture<SubmitSmResp> future3 = window.offer(createSubmitSm(3), Duration.ofSeconds(1));

        int cancelled = window.cancelAll(new RuntimeException("Session closed"));

        assertThat(cancelled).isEqualTo(3);
        assertThat(future1.isDone()).isTrue();
        assertThat(future2.isDone()).isTrue();
        assertThat(future3.isDone()).isTrue();
        assertThat(window.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("should close window and cancel all requests")
    void shouldCloseWindowAndCancelAllRequests() throws Exception {
        WindowFuture<SubmitSmResp> future = window.offer(createSubmitSm(1), Duration.ofSeconds(1));

        window.close();

        assertThat(window.isClosed()).isTrue();
        assertThat(future.isDone()).isTrue();
        assertThat(window.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("should reject offer on closed window")
    void shouldRejectOfferOnClosedWindow() {
        window.close();

        assertThatThrownBy(() -> window.offer(createSubmitSm(1), Duration.ofSeconds(1)))
                .hasMessageContaining("Window is closed");
    }

    @Test
    @DisplayName("should return null from tryOffer on closed window")
    void shouldReturnNullFromTryOfferOnClosedWindow() {
        window.close();

        WindowFuture<SubmitSmResp> future = window.tryOffer(createSubmitSm(1));

        assertThat(future).isNull();
    }

    @Test
    @DisplayName("should return pending futures")
    void shouldReturnPendingFutures() throws Exception {
        window.offer(createSubmitSm(1), Duration.ofSeconds(1));
        window.offer(createSubmitSm(2), Duration.ofSeconds(1));

        var futures = window.getPendingFutures();

        assertThat(futures).hasSize(2);
    }

    @Test
    @DisplayName("should handle concurrent offers correctly")
    void shouldHandleConcurrentOffersCorrectly() throws Exception {
        int threadCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int seq = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    WindowFuture<SubmitSmResp> future = window.tryOffer(createSubmitSm(seq));
                    if (future != null) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
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

        // Window size is 10, so 10 should succeed and 10 should fail
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(10);
        assertThat(window.isFull()).isTrue();
    }

    private SubmitSm createSubmitSm(int sequenceNumber) {
        return SubmitSm.builder()
                .sequenceNumber(sequenceNumber)
                .sourceAddress(Address.international("+14155551234"))
                .destAddress(Address.international("+14155555678"))
                .shortMessage("Test".getBytes())
                .build();
    }
}
