package com.omnixys.observability.autoconfigure;

import com.omnixys.observability.context.TraceContextExtractor;
import com.omnixys.observability.metrics.MetricsAutoConfiguration;
import com.omnixys.observability.properties.ObservabilityProperties;
import com.omnixys.observability.tracing.OpenTelemetryFactory;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Main auto-configuration entry for Omnixys Observability.
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ObservabilityProperties.class)
@ConditionalOnClass(OpenTelemetry.class)
@ConditionalOnProperty(prefix = "omnixys.observability", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ObservabilityAutoConfiguration {

    private final ObservabilityProperties properties;

    @Bean
    @ConditionalOnProperty(prefix = "omnixys.observability.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OpenTelemetry openTelemetry() {
        return OpenTelemetryFactory.create(properties);
    }
}