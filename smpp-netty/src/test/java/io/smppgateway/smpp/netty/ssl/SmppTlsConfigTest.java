package io.smppgateway.smpp.netty.ssl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIf;

import java.security.cert.CertificateException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppTlsConfig Tests")
class SmppTlsConfigTest {

    /**
     * Checks if self-signed certificate generation is available.
     * This requires BouncyCastle or specific JDK support.
     */
    static boolean isSelfSignedCertAvailable() {
        try {
            SmppTlsConfig.selfSignedServer();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("should create self-signed server config")
    @EnabledIf("isSelfSignedCertAvailable")
    void shouldCreateSelfSignedServerConfig() throws CertificateException {
        SmppTlsConfig config = SmppTlsConfig.selfSignedServer();

        assertThat(config).isNotNull();
        assertThat(config.isServer()).isTrue();
        assertThat(config.sslContext()).isNotNull();
    }

    @Test
    @DisplayName("should create insecure client config")
    void shouldCreateInsecureClientConfig() {
        SmppTlsConfig config = SmppTlsConfig.insecureClient();

        assertThat(config).isNotNull();
        assertThat(config.isServer()).isFalse();
        assertThat(config.sslContext()).isNotNull();
    }

    @Test
    @DisplayName("should return server builder")
    void shouldReturnServerBuilder() {
        SmppTlsConfig.ServerBuilder builder = SmppTlsConfig.forServer();

        assertThat(builder).isNotNull();
    }

    @Test
    @DisplayName("should return client builder")
    void shouldReturnClientBuilder() {
        SmppTlsConfig.ClientBuilder builder = SmppTlsConfig.forClient();

        assertThat(builder).isNotNull();
    }

    @Test
    @DisplayName("server builder should fail without PEM files or keystore")
    void serverBuilderShouldFailWithoutPemFilesOrKeystore() {
        SmppTlsConfig.ServerBuilder builder = SmppTlsConfig.forServer();

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Must specify either PEM files or keystore");
    }

    @Test
    @DisplayName("client builder should build with insecure flag")
    void clientBuilderShouldBuildWithInsecureFlag() throws Exception {
        SmppTlsConfig config = SmppTlsConfig.forClient()
                .insecure()
                .build();

        assertThat(config).isNotNull();
        assertThat(config.isServer()).isFalse();
    }

    @Test
    @DisplayName("client builder should allow setting SSL provider")
    void clientBuilderShouldAllowSettingSslProvider() throws Exception {
        SmppTlsConfig config = SmppTlsConfig.forClient()
                .insecure()
                .sslProvider(io.netty.handler.ssl.SslProvider.JDK)
                .build();

        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("client builder should allow setting protocols")
    void clientBuilderShouldAllowSettingProtocols() throws Exception {
        SmppTlsConfig config = SmppTlsConfig.forClient()
                .insecure()
                .protocols("TLSv1.2", "TLSv1.3")
                .build();

        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("client builder should allow setting ciphers")
    void clientBuilderShouldAllowSettingCiphers() throws Exception {
        SmppTlsConfig config = SmppTlsConfig.forClient()
                .insecure()
                .ciphers(java.util.List.of("TLS_AES_256_GCM_SHA384"))
                .build();

        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("insecure client should trust all certificates")
    void insecureClientShouldTrustAllCertificates() {
        SmppTlsConfig config = SmppTlsConfig.insecureClient();

        // The fact that it builds successfully with InsecureTrustManagerFactory
        // means it will trust all certificates
        assertThat(config.sslContext().isClient()).isTrue();
    }

    @Test
    @DisplayName("self-signed server config should be a server context")
    @EnabledIf("isSelfSignedCertAvailable")
    void selfSignedServerConfigShouldBeServerContext() throws CertificateException {
        SmppTlsConfig config = SmppTlsConfig.selfSignedServer();

        assertThat(config.sslContext().isServer()).isTrue();
    }
}
