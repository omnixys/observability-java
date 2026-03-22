package com.omnixys.observability.metrics;

import com.omnixys.observability.properties.ObservabilityProperties;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics configuration for Prometheus.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "omnixys.observability.metrics",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MetricsAutoConfiguration {

    private final ObservabilityProperties properties;

    @Bean
    @ConditionalOnProperty(
            prefix = "omnixys.observability.metrics",
            name = "prometheus-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
}