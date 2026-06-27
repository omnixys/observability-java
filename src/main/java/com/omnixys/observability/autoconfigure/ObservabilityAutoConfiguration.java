package com.omnixys.observability.autoconfigure;

import com.omnixys.observability.annotation.SpanAspect;
import com.omnixys.observability.api.TracePropagation;
import com.omnixys.observability.cache.CacheMetricsRecorder;
import com.omnixys.observability.cache.CacheTracing;
import com.omnixys.observability.context.CorrelationIdService;
import com.omnixys.observability.health.OpenTelemetryHealthIndicator;
import com.omnixys.observability.kafka.KafkaTracing;
import com.omnixys.observability.kafka.OpenTelemetryKafkaTracing;
import com.omnixys.observability.logging.ErrorClassifier;
import com.omnixys.observability.metrics.MetricsAutoConfiguration;
import com.omnixys.observability.metrics.RateLimitMetrics;
import com.omnixys.observability.metrics.SloMetricsService;
import com.omnixys.observability.otel.OpenTelemetryTracePropagation;
import com.omnixys.observability.properties.ObservabilityProperties;
import com.omnixys.observability.tracing.AdaptiveSampler;
import com.omnixys.observability.tracing.OpenTelemetryFactory;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ObservabilityProperties.class)
@ConditionalOnClass(OpenTelemetry.class)
@ConditionalOnProperty(prefix = "omnixys.observability", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ObservabilityAutoConfiguration {

    private final ObservabilityProperties properties;

    public ObservabilityAutoConfiguration(ObservabilityProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "omnixys.observability.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OpenTelemetry openTelemetry() {
        return OpenTelemetryFactory.create(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpanAspect spanAspect() {
        return new SpanAspect();
    }

    @Bean
    @ConditionalOnMissingBean(TracePropagation.class)
    public TracePropagation<Object> tracePropagation(OpenTelemetry openTelemetry) {
        return new OpenTelemetryTracePropagation(openTelemetry);
    }

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdService correlationIdService() {
        return new CorrelationIdService();
    }

    @Bean
    @ConditionalOnMissingBean
    public SloMetricsService sloMetricsService() {
        return new SloMetricsService();
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitMetrics rateLimitMetrics() {
        return new RateLimitMetrics();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheMetricsRecorder cacheMetricsRecorder() {
        return new CacheMetricsRecorder();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheTracing cacheTracing(Tracer tracer) {
        return new CacheTracing(tracer);
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaTracing kafkaTracing(Tracer tracer) {
        return new OpenTelemetryKafkaTracing(tracer);
    }

    @Bean
    @ConditionalOnMissingBean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("omnixys");
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetryHealthIndicator openTelemetryHealthIndicator(OpenTelemetry openTelemetry) {
        return new OpenTelemetryHealthIndicator(openTelemetry);
    }
}
