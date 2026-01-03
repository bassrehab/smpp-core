package io.smppgateway.smpp.server.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

@DisplayName("VirtualThreadExecutor Tests")
class VirtualThreadExecutorTest {

    @Test
    @DisplayName("should create executor with custom name prefix")
    @Timeout(5)
    void shouldCreateExecutorWithCustomNamePrefix() throws InterruptedException {
        ExecutorService executor = VirtualThreadExecutor.create("test-prefix");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();

        executor.execute(() -> {
            threadName.set(Thread.currentThread().getName());
            latch.countDown();
        });

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(threadName.get()).startsWith("test-prefix-");
        executor.shutdown();
    }

    @Test
    @DisplayName("should create executor for server handlers")
    @Timeout(5)
    void shouldCreateExecutorForServerHandlers() throws InterruptedException {
        ExecutorService executor = VirtualThreadExecutor.createForServer();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();

        executor.execute(() -> {
            threadName.set(Thread.currentThread().getName());
            latch.countDown();
        });

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(threadName.get()).startsWith("smpp-server-handler-");
        executor.shutdown();
    }

    @Test
    @DisplayName("should create executor for client handlers")
    @Timeout(5)
    void shouldCreateExecutorForClientHandlers() throws InterruptedException {
        ExecutorService executor = VirtualThreadExecutor.createForClient();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();

        executor.execute(() -> {
            threadName.set(Thread.currentThread().getName());
            latch.countDown();
        });

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(threadName.get()).startsWith("smpp-client-handler-");
        executor.shutdown();
    }

    @Test
    @DisplayName("should create simple virtual thread executor")
    @Timeout(5)
    void shouldCreateSimpleVirtualThreadExecutor() throws InterruptedException {
        Executor executor = VirtualThreadExecutor.simple();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean isVirtual = new AtomicBoolean(false);

        executor.execute(() -> {
            isVirtual.set(Thread.currentThread().isVirtual());
            latch.countDown();
        });

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(isVirtual.get()).isTrue();
    }

    @Test
    @DisplayName("should execute tasks on virtual threads")
    @Timeout(5)
    void shouldExecuteTasksOnVirtualThreads() throws InterruptedException {
        ExecutorService executor = VirtualThreadExecutor.create("virtual");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean isVirtual = new AtomicBoolean(false);

        executor.execute(() -> {
            isVirtual.set(Thread.currentThread().isVirtual());
            latch.countDown();
        });

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(isVirtual.get()).isTrue();
        executor.shutdown();
    }

    @Test
    @DisplayName("should wrap executor with exception logging")
    @Timeout(5)
    void shouldWrapExecutorWithExceptionLogging() throws InterruptedException {
        Executor baseExecutor = VirtualThreadExecutor.simple();
        Executor wrapped = VirtualThreadExecutor.withExceptionLogging(baseExecutor);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean executed = new AtomicBoolean(false);

        wrapped.execute(() -> {
            executed.set(true);
            latch.countDown();
        });

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(executed.get()).isTrue();
    }

    @Test
    @DisplayName("wrapped executor should rethrow exceptions")
    @Timeout(5)
    void wrappedExecutorShouldRethrowExceptions() throws InterruptedException {
        Executor baseExecutor = VirtualThreadExecutor.simple();
        Executor wrapped = VirtualThreadExecutor.withExceptionLogging(baseExecutor);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> caught = new AtomicReference<>();

        Thread.UncaughtExceptionHandler handler = (t, e) -> {
            caught.set(e);
            latch.countDown();
        };

        wrapped.execute(() -> {
            Thread.currentThread().setUncaughtExceptionHandler(handler);
            throw new RuntimeException("Test exception");
        });

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(caught.get()).isInstanceOf(RuntimeException.class);
        assertThat(caught.get().getMessage()).isEqualTo("Test exception");
    }

    @Test
    @DisplayName("should handle multiple concurrent tasks")
    @Timeout(10)
    void shouldHandleMultipleConcurrentTasks() throws InterruptedException {
        ExecutorService executor = VirtualThreadExecutor.create("concurrent");
        int taskCount = 100;
        CountDownLatch latch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            executor.execute(latch::countDown);
        }

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
    }

    @Test
    @DisplayName("executor should be shutdownable")
    @Timeout(5)
    void executorShouldBeShutdownable() {
        ExecutorService executor = VirtualThreadExecutor.create("shutdown-test");

        assertThat(executor.isShutdown()).isFalse();
        executor.shutdown();
        assertThat(executor.isShutdown()).isTrue();
    }
}
