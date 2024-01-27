package io.smppgateway.smpp.client;

import io.smppgateway.smpp.netty.ssl.SmppTlsConfig;
import io.smppgateway.smpp.types.SmppBindType;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable configuration for an SMPP client.
 */
public record SmppClientConfiguration(
    String host,
    int port,
    String systemId,
    String password,
    String systemType,
    SmppBindType bindType,
    byte interfaceVersion,
    byte addressTon,
    byte addressNpi,
    String addressRange,
    int windowSize,
    Duration connectTimeout,
    Duration bindTimeout,
    Duration requestTimeout,
    Duration enquireLinkInterval,
    Duration reconnectDelay,
    int maxReconnectAttempts,
    int maxPduSize,
    SmppTlsConfig tlsConfig
) {

    public SmppClientConfiguration {
        Objects.requireNonNull(host, "host must not be null");
        Objects.requireNonNull(systemId, "systemId must not be null");
        Objects.requireNonNull(bindType, "bindType must not be null");
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
     * Builder for client configuration.
     */
    public static class Builder {
        private String host = "localhost";
        private int port = 2775;
        private String systemId = "esme";
        private String password = "";
        private String systemType = "";
        private SmppBindType bindType = SmppBindType.TRANSCEIVER;
        private byte interfaceVersion = 0x34; // SMPP 3.4
        private byte addressTon = 0;
        private byte addressNpi = 0;
        private String addressRange = "";
        private int windowSize = 512;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration bindTimeout = Duration.ofSeconds(30);
        private Duration requestTimeout = Duration.ofSeconds(30);
        private Duration enquireLinkInterval = Duration.ofSeconds(30);
        private Duration reconnectDelay = Duration.ofSeconds(5);
        private int maxReconnectAttempts = -1; // -1 = infinite
        private int maxPduSize = 65536;
        private SmppTlsConfig tlsConfig = null;

        /**
         * Sets the server host.
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Sets the server port (default: 2775).
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the ESME system ID.
         */
        public Builder systemId(String systemId) {
            this.systemId = systemId;
            return this;
        }

        /**
         * Sets the password.
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the system type.
         */
        public Builder systemType(String systemType) {
            this.systemType = systemType;
            return this;
        }

        /**
         * Sets the bind type (default: TRANSCEIVER).
         */
        public Builder bindType(SmppBindType bindType) {
            this.bindType = bindType;
            return this;
        }

        /**
         * Sets the SMPP interface version (default: 0x34 for SMPP 3.4).
         */
        public Builder interfaceVersion(byte interfaceVersion) {
            this.interfaceVersion = interfaceVersion;
            return this;
        }

        /**
         * Sets the address TON.
         */
        public Builder addressTon(byte ton) {
            this.addressTon = ton;
            return this;
        }

        /**
         * Sets the address NPI.
         */
        public Builder addressNpi(byte npi) {
            this.addressNpi = npi;
            return this;
        }

        /**
         * Sets the address range.
         */
        public Builder addressRange(String range) {
            this.addressRange = range;
            return this;
        }

        /**
         * Sets the window size (default: 512).
         */
        public Builder windowSize(int windowSize) {
            this.windowSize = windowSize;
            return this;
        }

        /**
         * Sets the connection timeout (default: 10s).
         */
        public Builder connectTimeout(Duration timeout) {
            this.connectTimeout = timeout;
            return this;
        }

        /**
         * Sets the bind timeout (default: 30s).
         */
        public Builder bindTimeout(Duration timeout) {
            this.bindTimeout = timeout;
            return this;
        }

        /**
         * Sets the request timeout (default: 30s).
         */
        public Builder requestTimeout(Duration timeout) {
            this.requestTimeout = timeout;
            return this;
        }

        /**
         * Sets the enquire link interval (default: 30s).
         */
        public Builder enquireLinkInterval(Duration interval) {
            this.enquireLinkInterval = interval;
            return this;
        }

        /**
         * Sets the reconnect delay (default: 5s).
         */
        public Builder reconnectDelay(Duration delay) {
            this.reconnectDelay = delay;
            return this;
        }

        /**
         * Sets the max reconnect attempts (-1 for infinite, default: -1).
         */
        public Builder maxReconnectAttempts(int attempts) {
            this.maxReconnectAttempts = attempts;
            return this;
        }

        /**
         * Disables auto-reconnect.
         */
        public Builder noReconnect() {
            this.maxReconnectAttempts = 0;
            return this;
        }

        /**
         * Sets the maximum PDU size (default: 64KB).
         */
        public Builder maxPduSize(int size) {
            this.maxPduSize = size;
            return this;
        }

        /**
         * Enables TLS with the specified configuration.
         */
        public Builder tls(SmppTlsConfig tlsConfig) {
            this.tlsConfig = tlsConfig;
            return this;
        }

        public SmppClientConfiguration build() {
            return new SmppClientConfiguration(
                host, port, systemId, password, systemType, bindType,
                interfaceVersion, addressTon, addressNpi, addressRange,
                windowSize, connectTimeout, bindTimeout, requestTimeout,
                enquireLinkInterval, reconnectDelay, maxReconnectAttempts,
                maxPduSize, tlsConfig
            );
        }
    }
}
