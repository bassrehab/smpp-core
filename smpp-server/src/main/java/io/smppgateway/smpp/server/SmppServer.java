package io.smppgateway.smpp.server;

import io.smppgateway.smpp.netty.ssl.SmppTlsConfig;
import io.smppgateway.smpp.server.impl.DefaultSmppServer;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * SMPP Server interface.
 *
 * <p>An SMPP server accepts connections from ESME clients and processes
 * message submissions. The server uses virtual threads (Java 21) for
 * handling requests, enabling high concurrency with simple synchronous code.
 *
 * <p>Example usage:
 * <pre>
 * SmppServer server = SmppServer.builder()
 *     .port(2775)
 *     .systemId("my-smsc")
 *     .handler(new MyServerHandler())
 *     .build();
 *
 * server.start();
 *
 * // ... server is running ...
 *
 * server.stop();
 * </pre>
 */
public interface SmppServer {

    /**
     * Starts the server asynchronously.
     *
     * @return Future that completes when the server is started
     */
    CompletableFuture<Void> start();

    /**
     * Starts the server and waits for it to be ready.
     */
    void startSync();

    /**
     * Stops the server gracefully.
     *
     * @return Future that completes when the server is stopped
     */
    CompletableFuture<Void> stop();

    /**
     * Stops the server and waits for completion.
     */
    void stopSync();

    /**
     * Returns true if the server is running.
     */
    boolean isRunning();

    /**
     * Returns the port the server is listening on.
     */
    int getPort();

    /**
     * Returns all active sessions.
     */
    Collection<SmppServerSession> getSessions();

    /**
     * Returns a session by its ID.
     */
    SmppServerSession getSession(String sessionId);

    /**
     * Returns the number of active sessions.
     */
    int getSessionCount();

    /**
     * Returns the server configuration.
     */
    SmppServerConfiguration getConfiguration();

    /**
     * Creates a new server builder.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for SMPP servers.
     */
    class Builder {
        private int port = 2775;
        private String host = "0.0.0.0";
        private String systemId = "smsc";
        private SmppServerHandler handler;
        private int maxConnections = 1000;
        private int windowSize = 512;
        private Duration bindTimeout = Duration.ofSeconds(30);
        private Duration requestTimeout = Duration.ofSeconds(30);
        private Duration enquireLinkInterval = Duration.ofSeconds(30);
        private int maxPduSize = 65536;
        private SmppTlsConfig tlsConfig;
        private boolean useVirtualThreads = true;

        /**
         * Sets the server port (default: 2775).
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the bind address (default: 0.0.0.0).
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Sets the system ID returned in bind responses.
         */
        public Builder systemId(String systemId) {
            this.systemId = systemId;
            return this;
        }

        /**
         * Sets the server handler (required).
         */
        public Builder handler(SmppServerHandler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * Sets the maximum number of concurrent connections.
         */
        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        /**
         * Sets the window size for each connection.
         */
        public Builder windowSize(int windowSize) {
            this.windowSize = windowSize;
            return this;
        }

        /**
         * Sets the bind timeout.
         */
        public Builder bindTimeout(Duration bindTimeout) {
            this.bindTimeout = bindTimeout;
            return this;
        }

        /**
         * Sets the request timeout.
         */
        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        /**
         * Sets the enquire link interval.
         */
        public Builder enquireLinkInterval(Duration enquireLinkInterval) {
            this.enquireLinkInterval = enquireLinkInterval;
            return this;
        }

        /**
         * Sets the maximum PDU size.
         */
        public Builder maxPduSize(int maxPduSize) {
            this.maxPduSize = maxPduSize;
            return this;
        }

        /**
         * Enables TLS with the specified configuration.
         */
        public Builder tls(SmppTlsConfig tlsConfig) {
            this.tlsConfig = tlsConfig;
            return this;
        }

        /**
         * Sets whether to use virtual threads (default: true).
         */
        public Builder useVirtualThreads(boolean useVirtualThreads) {
            this.useVirtualThreads = useVirtualThreads;
            return this;
        }

        /**
         * Builds the server.
         */
        public SmppServer build() {
            if (handler == null) {
                throw new IllegalStateException("Handler must be set");
            }

            SmppServerConfiguration config = SmppServerConfiguration.builder()
                .port(port)
                .host(host)
                .systemId(systemId)
                .maxConnections(maxConnections)
                .windowSize(windowSize)
                .bindTimeout(bindTimeout)
                .requestTimeout(requestTimeout)
                .enquireLinkInterval(enquireLinkInterval)
                .maxPduSize(maxPduSize)
                .tls(tlsConfig)
                .useVirtualThreads(useVirtualThreads)
                .build();

            return new DefaultSmppServer(config, handler);
        }
    }
}
