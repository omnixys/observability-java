package com.omnixys.observability.metrics;

import com.omnixys.observability.cache.CacheMetricsRecorder;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        prefix = "omnixys.observability.metrics",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MetricsAutoConfiguration {

    @Configuration
    @ConditionalOnClass(name = "io.micrometer.prometheusmetrics.PrometheusMeterRegistry")
    @ConditionalOnProperty(
            prefix = "omnixys.observability.metrics",
            name = "prometheus-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    static class PrometheusMeterRegistryConfiguration {

        @Bean
        @ConditionalOnMissingBean(MeterRegistry.class)
        MeterRegistry prometheusMeterRegistry() {
            return new io.micrometer.prometheusmetrics.PrometheusMeterRegistry(
                    io.micrometer.prometheusmetrics.PrometheusConfig.DEFAULT
            );
        }
    }

    @Bean
    public MeterBinder sloMetricsBinder(SloMetricsService sloMetrics) {
        return registry -> {
            Gauge.builder("omnixys.slo.total", sloMetrics, SloMetricsService::total)
                    .description("Total SLO requests")
                    .register(registry);
            Gauge.builder("omnixys.slo.errors", sloMetrics, SloMetricsService::errors)
                    .description("Total SLO errors")
                    .register(registry);
            Gauge.builder("omnixys.slo.error.rate", sloMetrics, SloMetricsService::errorRate)
                    .description("SLO error rate")
                    .register(registry);
        };
    }

    @Bean
    public MeterBinder cacheMetricsBinder(CacheMetricsRecorder cacheMetrics) {
        return registry -> {
            Gauge.builder("omnixys.cache.hits", cacheMetrics, CacheMetricsRecorder::getHits)
                    .description("Cache hits")
                    .register(registry);
            Gauge.builder("omnixys.cache.misses", cacheMetrics, CacheMetricsRecorder::getMisses)
                    .description("Cache misses")
                    .register(registry);
            Gauge.builder("omnixys.cache.writes", cacheMetrics, CacheMetricsRecorder::getWrites)
                    .description("Cache writes")
                    .register(registry);
            Gauge.builder("omnixys.cache.deletes", cacheMetrics, CacheMetricsRecorder::getDeletes)
                    .description("Cache deletes")
                    .register(registry);
            Gauge.builder("omnixys.cache.errors", cacheMetrics, CacheMetricsRecorder::getErrors)
                    .description("Cache errors")
                    .register(registry);
        };
    }
}
