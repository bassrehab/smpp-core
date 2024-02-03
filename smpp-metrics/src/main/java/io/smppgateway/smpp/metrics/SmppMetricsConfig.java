package io.smppgateway.smpp.metrics;

import java.time.Duration;
import java.util.List;

/**
 * Configuration for SMPP metrics collection.
 *
 * <p>Controls which metrics are collected and how they're aggregated.
 */
public record SmppMetricsConfig(
    boolean enabled,
    String prefix,
    List<Double> percentiles,
    Duration[] slaBoundaries,
    boolean perSessionMetrics,
    boolean perCommandMetrics
) {

    /**
     * Creates a default configuration.
     */
    public static SmppMetricsConfig defaults() {
        return builder().build();
    }

    /**
     * Creates a builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for metrics configuration.
     */
    public static class Builder {
        private boolean enabled = true;
        private String prefix = "smpp";
        private List<Double> percentiles = List.of(0.5, 0.9, 0.95, 0.99);
        private Duration[] slaBoundaries = new Duration[] {
            Duration.ofMillis(10),
            Duration.ofMillis(50),
            Duration.ofMillis(100),
            Duration.ofMillis(500),
            Duration.ofSeconds(1),
            Duration.ofSeconds(5)
        };
        private boolean perSessionMetrics = false;
        private boolean perCommandMetrics = true;

        /**
         * Enables or disables metrics collection.
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the metric name prefix.
         */
        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Sets the percentiles to publish for histograms.
         */
        public Builder percentiles(List<Double> percentiles) {
            this.percentiles = percentiles;
            return this;
        }

        /**
         * Sets the SLA histogram boundaries.
         */
        public Builder slaBoundaries(Duration... boundaries) {
            this.slaBoundaries = boundaries;
            return this;
        }

        /**
         * Enables per-session metrics (higher cardinality).
         */
        public Builder perSessionMetrics(boolean enabled) {
            this.perSessionMetrics = enabled;
            return this;
        }

        /**
         * Enables per-command metrics (recommended).
         */
        public Builder perCommandMetrics(boolean enabled) {
            this.perCommandMetrics = enabled;
            return this;
        }

        public SmppMetricsConfig build() {
            return new SmppMetricsConfig(
                enabled, prefix, percentiles, slaBoundaries,
                perSessionMetrics, perCommandMetrics
            );
        }
    }
}
