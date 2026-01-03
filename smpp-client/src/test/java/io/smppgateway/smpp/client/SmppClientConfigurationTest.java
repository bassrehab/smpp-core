package io.smppgateway.smpp.client;

import io.smppgateway.smpp.types.SmppBindType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppClientConfiguration Tests")
class SmppClientConfigurationTest {

    @Test
    @DisplayName("should create configuration with default values")
    void shouldCreateConfigurationWithDefaultValues() {
        SmppClientConfiguration config = SmppClientConfiguration.builder().build();

        assertThat(config.host()).isEqualTo("localhost");
        assertThat(config.port()).isEqualTo(2775);
        assertThat(config.systemId()).isEqualTo("esme");
        assertThat(config.password()).isEmpty();
        assertThat(config.systemType()).isEmpty();
        assertThat(config.bindType()).isEqualTo(SmppBindType.TRANSCEIVER);
        assertThat(config.interfaceVersion()).isEqualTo((byte) 0x34);
        assertThat(config.windowSize()).isEqualTo(512);
        assertThat(config.connectTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(config.bindTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.requestTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.enquireLinkInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.reconnectDelay()).isEqualTo(Duration.ofSeconds(5));
        assertThat(config.maxReconnectAttempts()).isEqualTo(-1);
        assertThat(config.maxPduSize()).isEqualTo(65536);
        assertThat(config.tlsConfig()).isNull();
    }

    @Test
    @DisplayName("should create configuration with custom values")
    void shouldCreateConfigurationWithCustomValues() {
        SmppClientConfiguration config = SmppClientConfiguration.builder()
                .host("smpp.example.com")
                .port(2776)
                .systemId("my_esme")
                .password("secret123")
                .systemType("CMT")
                .bindType(SmppBindType.TRANSMITTER)
                .interfaceVersion((byte) 0x50)
                .addressTon((byte) 0x01)
                .addressNpi((byte) 0x01)
                .addressRange("123*")
                .windowSize(1000)
                .connectTimeout(Duration.ofSeconds(5))
                .bindTimeout(Duration.ofSeconds(15))
                .requestTimeout(Duration.ofSeconds(60))
                .enquireLinkInterval(Duration.ofSeconds(45))
                .reconnectDelay(Duration.ofSeconds(10))
                .maxReconnectAttempts(5)
                .maxPduSize(131072)
                .build();

        assertThat(config.host()).isEqualTo("smpp.example.com");
        assertThat(config.port()).isEqualTo(2776);
        assertThat(config.systemId()).isEqualTo("my_esme");
        assertThat(config.password()).isEqualTo("secret123");
        assertThat(config.systemType()).isEqualTo("CMT");
        assertThat(config.bindType()).isEqualTo(SmppBindType.TRANSMITTER);
        assertThat(config.interfaceVersion()).isEqualTo((byte) 0x50);
        assertThat(config.addressTon()).isEqualTo((byte) 0x01);
        assertThat(config.addressNpi()).isEqualTo((byte) 0x01);
        assertThat(config.addressRange()).isEqualTo("123*");
        assertThat(config.windowSize()).isEqualTo(1000);
        assertThat(config.maxReconnectAttempts()).isEqualTo(5);
    }

    @Test
    @DisplayName("should throw exception when host is null")
    void shouldThrowExceptionWhenHostIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> SmppClientConfiguration.builder().host(null).build())
                .withMessage("host must not be null");
    }

    @Test
    @DisplayName("should throw exception when systemId is null")
    void shouldThrowExceptionWhenSystemIdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> SmppClientConfiguration.builder().systemId(null).build())
                .withMessage("systemId must not be null");
    }

    @Test
    @DisplayName("should throw exception when bindType is null")
    void shouldThrowExceptionWhenBindTypeIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> SmppClientConfiguration.builder().bindType(null).build())
                .withMessage("bindType must not be null");
    }

    @Test
    @DisplayName("should throw exception when port is invalid")
    void shouldThrowExceptionWhenPortIsInvalid() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SmppClientConfiguration.builder().port(0).build())
                .withMessage("Port must be between 1 and 65535");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SmppClientConfiguration.builder().port(65536).build())
                .withMessage("Port must be between 1 and 65535");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SmppClientConfiguration.builder().port(-1).build())
                .withMessage("Port must be between 1 and 65535");
    }

    @Test
    @DisplayName("should throw exception when window size is invalid")
    void shouldThrowExceptionWhenWindowSizeIsInvalid() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SmppClientConfiguration.builder().windowSize(0).build())
                .withMessage("Window size must be at least 1");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SmppClientConfiguration.builder().windowSize(-1).build())
                .withMessage("Window size must be at least 1");
    }

    @Test
    @DisplayName("should accept valid port range boundaries")
    void shouldAcceptValidPortRangeBoundaries() {
        SmppClientConfiguration config1 = SmppClientConfiguration.builder().port(1).build();
        assertThat(config1.port()).isEqualTo(1);

        SmppClientConfiguration config2 = SmppClientConfiguration.builder().port(65535).build();
        assertThat(config2.port()).isEqualTo(65535);
    }

    @Test
    @DisplayName("noReconnect should set max attempts to 0")
    void noReconnectShouldSetMaxAttemptsToZero() {
        SmppClientConfiguration config = SmppClientConfiguration.builder()
                .noReconnect()
                .build();

        assertThat(config.maxReconnectAttempts()).isEqualTo(0);
    }

    @Test
    @DisplayName("should create configuration for different bind types")
    void shouldCreateConfigurationForDifferentBindTypes() {
        SmppClientConfiguration transmitter = SmppClientConfiguration.builder()
                .bindType(SmppBindType.TRANSMITTER)
                .build();
        assertThat(transmitter.bindType()).isEqualTo(SmppBindType.TRANSMITTER);

        SmppClientConfiguration receiver = SmppClientConfiguration.builder()
                .bindType(SmppBindType.RECEIVER)
                .build();
        assertThat(receiver.bindType()).isEqualTo(SmppBindType.RECEIVER);

        SmppClientConfiguration transceiver = SmppClientConfiguration.builder()
                .bindType(SmppBindType.TRANSCEIVER)
                .build();
        assertThat(transceiver.bindType()).isEqualTo(SmppBindType.TRANSCEIVER);
    }
}
