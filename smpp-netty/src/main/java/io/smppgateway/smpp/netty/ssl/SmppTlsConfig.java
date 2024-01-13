package io.smppgateway.smpp.netty.ssl;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration for SMPP TLS/SSL connections.
 *
 * <p>Supports multiple configuration modes:
 * <ul>
 *   <li>PEM files (certificate chain + private key)</li>
 *   <li>JKS/PKCS12 keystores</li>
 *   <li>Self-signed certificates (for testing)</li>
 *   <li>Insecure mode (trust all, for development only)</li>
 * </ul>
 *
 * <p>Example - Server with PEM files:
 * <pre>
 * SmppTlsConfig config = SmppTlsConfig.forServer()
 *     .certChainFile(new File("server.crt"))
 *     .keyFile(new File("server.key"))
 *     .keyPassword("secret")
 *     .build();
 * </pre>
 *
 * <p>Example - Client with keystore:
 * <pre>
 * SmppTlsConfig config = SmppTlsConfig.forClient()
 *     .trustStore(new File("truststore.jks"), "password")
 *     .build();
 * </pre>
 */
public class SmppTlsConfig {

    private final SslContext sslContext;
    private final boolean server;

    private SmppTlsConfig(SslContext sslContext, boolean server) {
        this.sslContext = sslContext;
        this.server = server;
    }

    /**
     * Returns the Netty SslContext.
     */
    public SslContext sslContext() {
        return sslContext;
    }

    /**
     * Returns true if this is a server configuration.
     */
    public boolean isServer() {
        return server;
    }

    /**
     * Creates a builder for server TLS configuration.
     */
    public static ServerBuilder forServer() {
        return new ServerBuilder();
    }

    /**
     * Creates a builder for client TLS configuration.
     */
    public static ClientBuilder forClient() {
        return new ClientBuilder();
    }

    /**
     * Creates a self-signed server configuration for testing.
     */
    public static SmppTlsConfig selfSignedServer() throws CertificateException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        try {
            SslContext ctx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                    .sslProvider(SslProvider.JDK)
                    .build();
            return new SmppTlsConfig(ctx, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create self-signed TLS config", e);
        }
    }

    /**
     * Creates an insecure client configuration that trusts all certificates.
     * WARNING: Only use for development/testing!
     */
    public static SmppTlsConfig insecureClient() {
        try {
            SslContext ctx = SslContextBuilder.forClient()
                    .sslProvider(SslProvider.JDK)
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            return new SmppTlsConfig(ctx, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create insecure TLS config", e);
        }
    }

    /**
     * Builder for server TLS configuration.
     */
    public static class ServerBuilder {
        private File certChainFile;
        private File keyFile;
        private String keyPassword;
        private File keyStoreFile;
        private String keyStorePassword;
        private String keyStoreType = "JKS";
        private SslProvider sslProvider = SslProvider.JDK;
        private List<String> protocols;
        private List<String> ciphers;

        /**
         * Sets the certificate chain file (PEM format).
         */
        public ServerBuilder certChainFile(File file) {
            this.certChainFile = file;
            return this;
        }

        /**
         * Sets the private key file (PEM format).
         */
        public ServerBuilder keyFile(File file) {
            this.keyFile = file;
            return this;
        }

        /**
         * Sets the private key password.
         */
        public ServerBuilder keyPassword(String password) {
            this.keyPassword = password;
            return this;
        }

        /**
         * Sets the keystore file.
         */
        public ServerBuilder keyStore(File file, String password) {
            this.keyStoreFile = file;
            this.keyStorePassword = password;
            return this;
        }

        /**
         * Sets the keystore type (default: JKS).
         */
        public ServerBuilder keyStoreType(String type) {
            this.keyStoreType = type;
            return this;
        }

        /**
         * Sets the SSL provider.
         */
        public ServerBuilder sslProvider(SslProvider provider) {
            this.sslProvider = provider;
            return this;
        }

        /**
         * Sets the allowed TLS protocols.
         */
        public ServerBuilder protocols(String... protocols) {
            this.protocols = Arrays.asList(protocols);
            return this;
        }

        /**
         * Sets the allowed cipher suites.
         */
        public ServerBuilder ciphers(List<String> ciphers) {
            this.ciphers = ciphers;
            return this;
        }

        public SmppTlsConfig build() throws Exception {
            SslContextBuilder builder;

            if (certChainFile != null && keyFile != null) {
                // PEM files
                builder = SslContextBuilder.forServer(certChainFile, keyFile, keyPassword);
            } else if (keyStoreFile != null) {
                // Keystore
                KeyManagerFactory kmf = loadKeyManagerFactory(keyStoreFile, keyStorePassword, keyStoreType);
                builder = SslContextBuilder.forServer(kmf);
            } else {
                throw new IllegalStateException("Must specify either PEM files or keystore");
            }

            builder.sslProvider(sslProvider);

            if (protocols != null) {
                builder.protocols(protocols);
            }
            if (ciphers != null) {
                builder.ciphers(ciphers);
            }

            return new SmppTlsConfig(builder.build(), true);
        }
    }

    /**
     * Builder for client TLS configuration.
     */
    public static class ClientBuilder {
        private File trustStoreFile;
        private String trustStorePassword;
        private String trustStoreType = "JKS";
        private File certChainFile;
        private File keyFile;
        private String keyPassword;
        private boolean insecure = false;
        private SslProvider sslProvider = SslProvider.JDK;
        private List<String> protocols;
        private List<String> ciphers;

        /**
         * Sets the truststore for server certificate validation.
         */
        public ClientBuilder trustStore(File file, String password) {
            this.trustStoreFile = file;
            this.trustStorePassword = password;
            return this;
        }

        /**
         * Sets the truststore type (default: JKS).
         */
        public ClientBuilder trustStoreType(String type) {
            this.trustStoreType = type;
            return this;
        }

        /**
         * Sets the client certificate chain (for mutual TLS).
         */
        public ClientBuilder certChainFile(File file) {
            this.certChainFile = file;
            return this;
        }

        /**
         * Sets the client private key (for mutual TLS).
         */
        public ClientBuilder keyFile(File file) {
            this.keyFile = file;
            return this;
        }

        /**
         * Sets the client private key password.
         */
        public ClientBuilder keyPassword(String password) {
            this.keyPassword = password;
            return this;
        }

        /**
         * Disables certificate validation. WARNING: Only for development!
         */
        public ClientBuilder insecure() {
            this.insecure = true;
            return this;
        }

        /**
         * Sets the SSL provider.
         */
        public ClientBuilder sslProvider(SslProvider provider) {
            this.sslProvider = provider;
            return this;
        }

        /**
         * Sets the allowed TLS protocols.
         */
        public ClientBuilder protocols(String... protocols) {
            this.protocols = Arrays.asList(protocols);
            return this;
        }

        /**
         * Sets the allowed cipher suites.
         */
        public ClientBuilder ciphers(List<String> ciphers) {
            this.ciphers = ciphers;
            return this;
        }

        public SmppTlsConfig build() throws Exception {
            SslContextBuilder builder = SslContextBuilder.forClient();

            // Trust configuration
            if (insecure) {
                builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
            } else if (trustStoreFile != null) {
                TrustManagerFactory tmf = loadTrustManagerFactory(trustStoreFile, trustStorePassword, trustStoreType);
                builder.trustManager(tmf);
            }

            // Client certificate (mutual TLS)
            if (certChainFile != null && keyFile != null) {
                builder.keyManager(certChainFile, keyFile, keyPassword);
            }

            builder.sslProvider(sslProvider);

            if (protocols != null) {
                builder.protocols(protocols);
            }
            if (ciphers != null) {
                builder.ciphers(ciphers);
            }

            return new SmppTlsConfig(builder.build(), false);
        }
    }

    private static KeyManagerFactory loadKeyManagerFactory(File keyStoreFile, String password, String type)
            throws Exception {
        KeyStore keyStore = KeyStore.getInstance(type);
        try (InputStream is = new FileInputStream(keyStoreFile)) {
            keyStore.load(is, password.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password.toCharArray());
        return kmf;
    }

    private static TrustManagerFactory loadTrustManagerFactory(File trustStoreFile, String password, String type)
            throws Exception {
        KeyStore trustStore = KeyStore.getInstance(type);
        try (InputStream is = new FileInputStream(trustStoreFile)) {
            trustStore.load(is, password.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf;
    }
}
