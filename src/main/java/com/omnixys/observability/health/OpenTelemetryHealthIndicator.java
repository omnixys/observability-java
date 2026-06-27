package com.omnixys.observability.health;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

public class OpenTelemetryHealthIndicator implements HealthIndicator {

    private final OpenTelemetry openTelemetry;

    public OpenTelemetryHealthIndicator(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public Health health() {
        try {
            Tracer tracer = openTelemetry.getTracer("omnixys.health");
            tracer.spanBuilder("health-check").startSpan().end();
            return Health.up()
                    .withDetail("otel", "initialized")
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("otel", "unavailable")
                    .build();
        }
    }
}
