package io.smppgateway.smpp.client;

import io.smppgateway.smpp.client.impl.DefaultSmppClient;
import io.smppgateway.smpp.netty.ssl.SmppTlsConfig;
import io.smppgateway.smpp.types.SmppBindType;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * SMPP Client interface.
 *
 * <p>An SMPP client connects to an SMSC and can send/receive messages.
 * The client supports automatic reconnection and provides both synchronous
 * and asynchronous APIs.
 *
 * <p>Example usage:
 * <pre>
 * SmppClient client = SmppClient.builder()
 *     .host("smsc.example.com")
 *     .port(2775)
 *     .systemId("my-esme")
 *     .password("secret")
 *     .bindType(SmppBindType.TRANSCEIVER)
 *     .handler(new MyHandler())
 *     .build();
 *
 * // Connect and get session
 * SmppClientSession session = client.connect();
 *
 * // Send message
 * SubmitSmResp resp = session.submitSm(submitSm, Duration.ofSeconds(10));
 *
 * // Disconnect
 * client.disconnect();
 * </pre>
 */
public interface SmppClient {

    /**
     * Connects to the SMSC and binds.
     *
     * @return The connected session
     * @throws Exception If connection or bind fails
     */
    SmppClientSession connect() throws Exception;

    /**
     * Connects asynchronously.
     *
     * @return Future that completes with the session
     */
    CompletableFuture<SmppClientSession> connectAsync();

    /**
     * Disconnects from the SMSC.
     */
    void disconnect();

    /**
     * Disconnects asynchronously.
     */
    CompletableFuture<Void> disconnectAsync();

    /**
     * Returns the current session, or null if not connected.
     */
    SmppClientSession getSession();

    /**
     * Returns true if connected and bound.
     */
    boolean isConnected();

    /**
     * Returns the client configuration.
     */
    SmppClientConfiguration getConfiguration();

    /**
     * Creates a new client builder.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for SMPP clients.
     */
    class Builder {
        private String host = "localhost";
        private int port = 2775;
        private String systemId = "esme";
        private String password = "";
        private String systemType = "";
        private SmppBindType bindType = SmppBindType.TRANSCEIVER;
        private SmppClientHandler handler;
        private int windowSize = 512;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration bindTimeout = Duration.ofSeconds(30);
        private Duration requestTimeout = Duration.ofSeconds(30);
        private Duration enquireLinkInterval = Duration.ofSeconds(30);
        private Duration reconnectDelay = Duration.ofSeconds(5);
        private int maxReconnectAttempts = -1;
        private int maxPduSize = 65536;
        private SmppTlsConfig tlsConfig;

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
         * Sets the handler (required).
         */
        public Builder handler(SmppClientHandler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * Sets the window size.
         */
        public Builder windowSize(int windowSize) {
            this.windowSize = windowSize;
            return this;
        }

        /**
         * Sets the connection timeout.
         */
        public Builder connectTimeout(Duration timeout) {
            this.connectTimeout = timeout;
            return this;
        }

        /**
         * Sets the bind timeout.
         */
        public Builder bindTimeout(Duration timeout) {
            this.bindTimeout = timeout;
            return this;
        }

        /**
         * Sets the request timeout.
         */
        public Builder requestTimeout(Duration timeout) {
            this.requestTimeout = timeout;
            return this;
        }

        /**
         * Sets the enquire link interval.
         */
        public Builder enquireLinkInterval(Duration interval) {
            this.enquireLinkInterval = interval;
            return this;
        }

        /**
         * Enables auto-reconnect with the specified delay.
         */
        public Builder withAutoReconnect(Duration delay) {
            this.reconnectDelay = delay;
            this.maxReconnectAttempts = -1;
            return this;
        }

        /**
         * Enables auto-reconnect with limited attempts.
         */
        public Builder withAutoReconnect(Duration delay, int maxAttempts) {
            this.reconnectDelay = delay;
            this.maxReconnectAttempts = maxAttempts;
            return this;
        }

        /**
         * Disables auto-reconnect.
         */
        public Builder noAutoReconnect() {
            this.maxReconnectAttempts = 0;
            return this;
        }

        /**
         * Sets the maximum PDU size.
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

        /**
         * Builds the client.
         */
        public SmppClient build() {
            if (handler == null) {
                throw new IllegalStateException("Handler must be set");
            }

            SmppClientConfiguration config = SmppClientConfiguration.builder()
                .host(host)
                .port(port)
                .systemId(systemId)
                .password(password)
                .systemType(systemType)
                .bindType(bindType)
                .windowSize(windowSize)
                .connectTimeout(connectTimeout)
                .bindTimeout(bindTimeout)
                .requestTimeout(requestTimeout)
                .enquireLinkInterval(enquireLinkInterval)
                .reconnectDelay(reconnectDelay)
                .maxReconnectAttempts(maxReconnectAttempts)
                .maxPduSize(maxPduSize)
                .tls(tlsConfig)
                .build();

            return new DefaultSmppClient(config, handler);
        }
    }
}
