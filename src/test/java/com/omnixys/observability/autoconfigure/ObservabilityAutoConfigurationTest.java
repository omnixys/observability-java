package com.omnixys.observability.autoconfigure;

import com.omnixys.observability.annotation.SpanAspect;
import com.omnixys.observability.cache.CacheMetricsRecorder;
import com.omnixys.observability.context.CorrelationIdService;
import com.omnixys.observability.metrics.RateLimitMetrics;
import com.omnixys.observability.metrics.SloMetricsService;
import com.omnixys.observability.properties.ObservabilityProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObservabilityAutoConfigurationTest {

    private final ObservabilityProperties properties = new ObservabilityProperties();
    private final ObservabilityAutoConfiguration config = new ObservabilityAutoConfiguration(properties);

    @Test
    void shouldCreateCorrelationIdServiceBean() {
        assertNotNull(config.correlationIdService());
    }

    @Test
    void shouldCreateSpanAspectBean() {
        assertNotNull(config.spanAspect());
    }

    @Test
    void shouldCreateRateLimitMetricsBean() {
        assertNotNull(config.rateLimitMetrics());
    }

    @Test
    void shouldCreateSloMetricsServiceBean() {
        assertNotNull(config.sloMetricsService());
    }

    @Test
    void shouldCreateCacheMetricsRecorderBean() {
        assertNotNull(config.cacheMetricsRecorder());
    }
}
