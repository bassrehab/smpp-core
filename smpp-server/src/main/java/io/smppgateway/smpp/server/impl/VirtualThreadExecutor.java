package io.smppgateway.smpp.server.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Factory for creating virtual thread executors (Java 21).
 *
 * <p>Virtual threads are lightweight threads that enable high concurrency
 * with simple synchronous code. They're ideal for SMPP handlers where
 * each request can be processed in a blocking manner without consuming
 * a platform thread.
 */
public final class VirtualThreadExecutor {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadExecutor.class);

    private VirtualThreadExecutor() {}

    /**
     * Creates a virtual thread executor with a custom name prefix.
     *
     * @param namePrefix Prefix for thread names
     * @return An executor that runs tasks on virtual threads
     */
    public static ExecutorService create(String namePrefix) {
        AtomicLong counter = new AtomicLong(0);
        ThreadFactory factory = Thread.ofVirtual()
            .name(namePrefix + "-", counter.getAndIncrement())
            .factory();

        return Executors.newThreadPerTaskExecutor(factory);
    }

    /**
     * Creates a virtual thread executor for SMPP server handlers.
     */
    public static ExecutorService createForServer() {
        return create("smpp-server-handler");
    }

    /**
     * Creates a virtual thread executor for SMPP client handlers.
     */
    public static ExecutorService createForClient() {
        return create("smpp-client-handler");
    }

    /**
     * Creates a simple virtual thread executor without naming.
     */
    public static Executor simple() {
        return command -> Thread.startVirtualThread(command);
    }

    /**
     * Wraps an executor to catch and log exceptions.
     */
    public static Executor withExceptionLogging(Executor executor) {
        return command -> executor.execute(() -> {
            try {
                command.run();
            } catch (Throwable t) {
                log.error("Uncaught exception in virtual thread", t);
                throw t;
            }
        });
    }
}
