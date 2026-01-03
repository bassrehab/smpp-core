package io.smppgateway.smpp.netty.window;

import io.smppgateway.smpp.pdu.SubmitSm;
import io.smppgateway.smpp.pdu.SubmitSmResp;
import io.smppgateway.smpp.types.Address;
import io.smppgateway.smpp.types.CommandStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("WindowFuture Tests")
class WindowFutureTest {

    @Test
    @DisplayName("should create future with request and sequence number")
    void shouldCreateFutureWithRequestAndSequenceNumber() {
        SubmitSm request = createSubmitSm(42);

        WindowFuture<SubmitSmResp> future = new WindowFuture<>(request, 42);

        assertThat(future.getRequest()).isEqualTo(request);
        assertThat(future.getSequenceNumber()).isEqualTo(42);
        assertThat(future.isDone()).isFalse();
        assertThat(future.isCancelled()).isFalse();
        assertThat(future.getCreatedAt()).isNotNull();
        assertThat(future.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("should complete with response")
    void shouldCompleteWithResponse() {
        SubmitSm request = createSubmitSm(42);
        WindowFuture<SubmitSmResp> future = new WindowFuture<>(request, 42);
        SubmitSmResp response = new SubmitSmResp(42, CommandStatus.OK, "MSG123", null);

        future.complete(response);

        assertThat(future.isDone()).isTrue();
        assertThat(future.getFuture().join()).isEqualTo(response);
        assertThat(future.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("should complete exceptionally")
    void shouldCompleteExceptionally() {
        WindowFuture<SubmitSmResp> future = new WindowFuture<>(createSubmitSm(42), 42);
        RuntimeException error = new RuntimeException("Connection lost");

        future.completeExceptionally(error);

        assertThat(future.isDone()).isTrue();
        assertThat(future.getCompletedAt()).isNotNull();
        assertThatThrownBy(() -> future.getFuture().join())
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("should cancel future")
    void shouldCancelFuture() {
        WindowFuture<SubmitSmResp> future = new WindowFuture<>(createSubmitSm(42), 42);

        boolean cancelled = future.cancel();

        assertThat(cancelled).isTrue();
        assertThat(future.isCancelled()).isTrue();
        assertThat(future.isDone()).isTrue();
        assertThat(future.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("should await response with timeout")
    void shouldAwaitResponseWithTimeout() throws Exception {
        WindowFuture<SubmitSmResp> future = new WindowFuture<>(createSubmitSm(42), 42);
        SubmitSmResp response = new SubmitSmResp(42, CommandStatus.OK, "MSG123", null);

        // Complete in background
        Thread completer = new Thread(() -> {
            try {
                Thread.sleep(50);
                future.complete(response);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        completer.start();

        SubmitSmResp result = future.await(1, TimeUnit.SECONDS);

        assertThat(result).isEqualTo(response);
        completer.join();
    }

    @Test
    @DisplayName("should throw TimeoutException when await times out")
    void shouldThrowTimeoutExceptionWhenAwaitTimesOut() {
        WindowFuture<SubmitSmResp> future = new WindowFuture<>(createSubmitSm(42), 42);

        assertThatThrownBy(() -> future.await(50, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);
    }

    @Test
    @DisplayName("should throw RuntimeException when await encounters exception")
    void shouldThrowRuntimeExceptionWhenAwaitEncountersException() {
        WindowFuture<SubmitSmResp> future = new WindowFuture<>(createSubmitSm(42), 42);
        future.completeExceptionally(new RuntimeException("Test error"));

        assertThatThrownBy(() -> future.await(1, TimeUnit.SECONDS))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test error");
    }

    @Test
    @DisplayName("should calculate elapsed time after completion")
    void shouldCalculateElapsedTimeAfterCompletion() throws Exception {
        WindowFuture<SubmitSmResp> future = new WindowFuture<>(createSubmitSm(42), 42);

        // Before completion
        assertThat(future.getElapsedMillis()).isEqualTo(-1);

        Thread.sleep(50);
        future.complete(new SubmitSmResp(42, CommandStatus.OK, null, null));

        // After completion
        assertThat(future.getElapsedMillis()).isGreaterThanOrEqualTo(50);
    }

    @Test
    @DisplayName("should calculate age correctly")
    void shouldCalculateAgeCorrectly() throws Exception {
        WindowFuture<SubmitSmResp> future = new WindowFuture<>(createSubmitSm(42), 42);

        Thread.sleep(50);

        assertThat(future.getAgeMillis()).isGreaterThanOrEqualTo(50);
    }

    @Test
    @DisplayName("should return underlying CompletableFuture")
    void shouldReturnUnderlyingCompletableFuture() {
        WindowFuture<SubmitSmResp> future = new WindowFuture<>(createSubmitSm(42), 42);

        assertThat(future.getFuture()).isNotNull();
        assertThat(future.getFuture().isDone()).isFalse();
    }

    @Test
    @DisplayName("should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        WindowFuture<SubmitSmResp> future = new WindowFuture<>(createSubmitSm(42), 42);

        String result = future.toString();

        assertThat(result).contains("seq=42");
        assertThat(result).contains("SUBMIT_SM");
        assertThat(result).contains("done=false");
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
