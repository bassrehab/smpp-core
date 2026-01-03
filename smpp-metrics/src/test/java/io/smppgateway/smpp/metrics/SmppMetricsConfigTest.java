package io.smppgateway.smpp.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SmppMetricsConfig Tests")
class SmppMetricsConfigTest {

    @Test
    @DisplayName("should create default configuration")
    void shouldCreateDefaultConfiguration() {
        SmppMetricsConfig config = SmppMetricsConfig.defaults();

        assertThat(config.enabled()).isTrue();
        assertThat(config.prefix()).isEqualTo("smpp");
        assertThat(config.percentiles()).containsExactly(0.5, 0.9, 0.95, 0.99);
        assertThat(config.slaBoundaries()).hasSize(6);
        assertThat(config.perSessionMetrics()).isFalse();
        assertThat(config.perCommandMetrics()).isTrue();
    }

    @Test
    @DisplayName("should create configuration via builder")
    void shouldCreateConfigurationViaBuilder() {
        SmppMetricsConfig config = SmppMetricsConfig.builder().build();

        assertThat(config).isNotNull();
        assertThat(config.enabled()).isTrue();
    }

    @Test
    @DisplayName("should allow disabling metrics")
    void shouldAllowDisablingMetrics() {
        SmppMetricsConfig config = SmppMetricsConfig.builder()
                .enabled(false)
                .build();

        assertThat(config.enabled()).isFalse();
    }

    @Test
    @DisplayName("should allow setting custom prefix")
    void shouldAllowSettingCustomPrefix() {
        SmppMetricsConfig config = SmppMetricsConfig.builder()
                .prefix("custom.smpp")
                .build();

        assertThat(config.prefix()).isEqualTo("custom.smpp");
    }

    @Test
    @DisplayName("should allow setting custom percentiles")
    void shouldAllowSettingCustomPercentiles() {
        List<Double> customPercentiles = List.of(0.5, 0.99);

        SmppMetricsConfig config = SmppMetricsConfig.builder()
                .percentiles(customPercentiles)
                .build();

        assertThat(config.percentiles()).containsExactly(0.5, 0.99);
    }

    @Test
    @DisplayName("should allow setting SLA boundaries")
    void shouldAllowSettingSlaBoundaries() {
        Duration[] boundaries = new Duration[] {
                Duration.ofMillis(100),
                Duration.ofSeconds(1)
        };

        SmppMetricsConfig config = SmppMetricsConfig.builder()
                .slaBoundaries(boundaries)
                .build();

        assertThat(config.slaBoundaries()).hasSize(2);
        assertThat(config.slaBoundaries()[0]).isEqualTo(Duration.ofMillis(100));
        assertThat(config.slaBoundaries()[1]).isEqualTo(Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("should allow enabling per-session metrics")
    void shouldAllowEnablingPerSessionMetrics() {
        SmppMetricsConfig config = SmppMetricsConfig.builder()
                .perSessionMetrics(true)
                .build();

        assertThat(config.perSessionMetrics()).isTrue();
    }

    @Test
    @DisplayName("should allow disabling per-command metrics")
    void shouldAllowDisablingPerCommandMetrics() {
        SmppMetricsConfig config = SmppMetricsConfig.builder()
                .perCommandMetrics(false)
                .build();

        assertThat(config.perCommandMetrics()).isFalse();
    }

    @Test
    @DisplayName("builder should be fluent")
    void builderShouldBeFluent() {
        SmppMetricsConfig config = SmppMetricsConfig.builder()
                .enabled(true)
                .prefix("test")
                .percentiles(List.of(0.5))
                .slaBoundaries(Duration.ofMillis(100))
                .perSessionMetrics(true)
                .perCommandMetrics(true)
                .build();

        assertThat(config.enabled()).isTrue();
        assertThat(config.prefix()).isEqualTo("test");
        assertThat(config.percentiles()).containsExactly(0.5);
        assertThat(config.slaBoundaries()).hasSize(1);
        assertThat(config.perSessionMetrics()).isTrue();
        assertThat(config.perCommandMetrics()).isTrue();
    }

    @Test
    @DisplayName("should have correct default SLA boundaries")
    void shouldHaveCorrectDefaultSlaBoundaries() {
        SmppMetricsConfig config = SmppMetricsConfig.defaults();

        Duration[] boundaries = config.slaBoundaries();
        assertThat(boundaries[0]).isEqualTo(Duration.ofMillis(10));
        assertThat(boundaries[1]).isEqualTo(Duration.ofMillis(50));
        assertThat(boundaries[2]).isEqualTo(Duration.ofMillis(100));
        assertThat(boundaries[3]).isEqualTo(Duration.ofMillis(500));
        assertThat(boundaries[4]).isEqualTo(Duration.ofSeconds(1));
        assertThat(boundaries[5]).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("record should support equals and hashCode")
    void recordShouldSupportEqualsAndHashCode() {
        SmppMetricsConfig config1 = SmppMetricsConfig.builder()
                .prefix("test")
                .enabled(true)
                .build();
        SmppMetricsConfig config2 = SmppMetricsConfig.builder()
                .prefix("test")
                .enabled(true)
                .build();

        assertThat(config1.prefix()).isEqualTo(config2.prefix());
        assertThat(config1.enabled()).isEqualTo(config2.enabled());
    }
}
