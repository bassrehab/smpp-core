package io.smppgateway.smpp.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppServerConfiguration Tests")
class SmppServerConfigurationTest {

    @Test
    @DisplayName("should create configuration with default values")
    void shouldCreateConfigurationWithDefaultValues() {
        SmppServerConfiguration config = SmppServerConfiguration.builder().build();

        assertThat(config.port()).isEqualTo(2775);
        assertThat(config.host()).isEqualTo("0.0.0.0");
        assertThat(config.systemId()).isEqualTo("smsc");
        assertThat(config.maxConnections()).isEqualTo(1000);
        assertThat(config.windowSize()).isEqualTo(512);
        assertThat(config.bindTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.requestTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.enquireLinkInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.maxPduSize()).isEqualTo(65536);
        assertThat(config.tlsConfig()).isNull();
        assertThat(config.useVirtualThreads()).isTrue();
    }

    @Test
    @DisplayName("should create configuration with custom values")
    void shouldCreateConfigurationWithCustomValues() {
        SmppServerConfiguration config = SmppServerConfiguration.builder()
                .port(2776)
                .host("127.0.0.1")
                .systemId("my_smsc")
                .maxConnections(5000)
                .windowSize(1024)
                .bindTimeout(Duration.ofSeconds(60))
                .requestTimeout(Duration.ofSeconds(45))
                .enquireLinkInterval(Duration.ofSeconds(15))
                .maxPduSize(131072)
                .useVirtualThreads(false)
                .build();

        assertThat(config.port()).isEqualTo(2776);
        assertThat(config.host()).isEqualTo("127.0.0.1");
        assertThat(config.systemId()).isEqualTo("my_smsc");
        assertThat(config.maxConnections()).isEqualTo(5000);
        assertThat(config.windowSize()).isEqualTo(1024);
        assertThat(config.bindTimeout()).isEqualTo(Duration.ofSeconds(60));
        assertThat(config.requestTimeout()).isEqualTo(Duration.ofSeconds(45));
        assertThat(config.enquireLinkInterval()).isEqualTo(Duration.ofSeconds(15));
        assertThat(config.maxPduSize()).isEqualTo(131072);
        assertThat(config.useVirtualThreads()).isFalse();
    }

    @Test
    @DisplayName("should throw exception when systemId is null")
    void shouldThrowExceptionWhenSystemIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> SmppServerConfiguration.builder().systemId(null).build())
                .withMessage("systemId must not be null");
    }

    @Test
    @DisplayName("should throw exception when port is invalid")
    void shouldThrowExceptionWhenPortIsInvalid() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SmppServerConfiguration.builder().port(0).build())
                .withMessage("Port must be between 1 and 65535");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SmppServerConfiguration.builder().port(65536).build())
                .withMessage("Port must be between 1 and 65535");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SmppServerConfiguration.builder().port(-1).build())
                .withMessage("Port must be between 1 and 65535");
    }

    @Test
    @DisplayName("should throw exception when window size is invalid")
    void shouldThrowExceptionWhenWindowSizeIsInvalid() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SmppServerConfiguration.builder().windowSize(0).build())
                .withMessage("Window size must be at least 1");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SmppServerConfiguration.builder().windowSize(-1).build())
                .withMessage("Window size must be at least 1");
    }

    @Test
    @DisplayName("should accept valid port range boundaries")
    void shouldAcceptValidPortRangeBoundaries() {
        SmppServerConfiguration config1 = SmppServerConfiguration.builder().port(1).build();
        assertThat(config1.port()).isEqualTo(1);

        SmppServerConfiguration config2 = SmppServerConfiguration.builder().port(65535).build();
        assertThat(config2.port()).isEqualTo(65535);
    }

    @Test
    @DisplayName("should accept minimum window size")
    void shouldAcceptMinimumWindowSize() {
        SmppServerConfiguration config = SmppServerConfiguration.builder().windowSize(1).build();
        assertThat(config.windowSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("should bind to all interfaces by default")
    void shouldBindToAllInterfacesByDefault() {
        SmppServerConfiguration config = SmppServerConfiguration.builder().build();
        assertThat(config.host()).isEqualTo("0.0.0.0");
    }

    @Test
    @DisplayName("should use virtual threads by default")
    void shouldUseVirtualThreadsByDefault() {
        SmppServerConfiguration config = SmppServerConfiguration.builder().build();
        assertThat(config.useVirtualThreads()).isTrue();
    }

    @Test
    @DisplayName("should allow disabling virtual threads")
    void shouldAllowDisablingVirtualThreads() {
        SmppServerConfiguration config = SmppServerConfiguration.builder()
                .useVirtualThreads(false)
                .build();
        assertThat(config.useVirtualThreads()).isFalse();
    }
}
