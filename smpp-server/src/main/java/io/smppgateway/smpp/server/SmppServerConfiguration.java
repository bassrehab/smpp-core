package io.smppgateway.smpp.server;

import io.smppgateway.smpp.netty.ssl.SmppTlsConfig;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable configuration for an SMPP server.
 */
public record SmppServerConfiguration(
    int port,
    String host,
    String systemId,
    int maxConnections,
    int windowSize,
    Duration bindTimeout,
    Duration requestTimeout,
    Duration enquireLinkInterval,
    int maxPduSize,
    SmppTlsConfig tlsConfig,
    boolean useVirtualThreads
) {

    public SmppServerConfiguration {
        Objects.requireNonNull(systemId, "systemId must not be null");
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        if (windowSize < 1) {
            throw new IllegalArgumentException("Window size must be at least 1");
        }
    }

    /**
     * Creates a builder with default values.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for server configuration.
     */
    public static class Builder {
        private int port = 2775;
        private String host = "0.0.0.0";
        private String systemId = "smsc";
        private int maxConnections = 1000;
        private int windowSize = 512;
        private Duration bindTimeout = Duration.ofSeconds(30);
        private Duration requestTimeout = Duration.ofSeconds(30);
        private Duration enquireLinkInterval = Duration.ofSeconds(30);
        private int maxPduSize = 65536;
        private SmppTlsConfig tlsConfig = null;
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
         * Sets the maximum number of concurrent connections (default: 1000).
         */
        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        /**
         * Sets the window size for each connection (default: 512).
         */
        public Builder windowSize(int windowSize) {
            this.windowSize = windowSize;
            return this;
        }

        /**
         * Sets the bind timeout (default: 30s).
         */
        public Builder bindTimeout(Duration bindTimeout) {
            this.bindTimeout = bindTimeout;
            return this;
        }

        /**
         * Sets the request timeout (default: 30s).
         */
        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        /**
         * Sets the enquire link interval (default: 30s).
         */
        public Builder enquireLinkInterval(Duration enquireLinkInterval) {
            this.enquireLinkInterval = enquireLinkInterval;
            return this;
        }

        /**
         * Sets the maximum PDU size (default: 64KB).
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

        public SmppServerConfiguration build() {
            return new SmppServerConfiguration(
                port, host, systemId, maxConnections, windowSize,
                bindTimeout, requestTimeout, enquireLinkInterval,
                maxPduSize, tlsConfig, useVirtualThreads
            );
        }
    }
}
