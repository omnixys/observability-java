package com.omnixys.observability.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Central Omnixys Observability configuration.
 * Provides a clean abstraction over Spring Boot and OpenTelemetry configuration.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "omnixys.observability")
public class ObservabilityProperties {

    private boolean enabled = true;

    private String serviceName = "unknown-service";

    private Tracing tracing = new Tracing();

    private Metrics metrics = new Metrics();

    private Otlp otlp = new Otlp();

    private Map<String, String> resourceAttributes = new HashMap<>();

    // ---------------------------------------------------------------------
    // Getters / Setters
    // ---------------------------------------------------------------------

    // ---------------------------------------------------------------------
    // Nested Classes
    // ---------------------------------------------------------------------

    @Getter
    public static class Tracing {

        @Setter
        private boolean enabled = true;

        private double samplingProbability = 1.0;

        @Setter
        private String propagation = "w3c";

        public void setSamplingProbability(double samplingProbability) {
            if (samplingProbability < 0.0 || samplingProbability > 1.0) {
                throw new IllegalArgumentException("Sampling probability must be between 0.0 and 1.0");
            }
            this.samplingProbability = samplingProbability;
        }

    }

    @Setter
    @Getter
    public static class Metrics {

        private boolean enabled = true;

        private boolean prometheusEnabled = true;

    }

    @Getter
    public static class Otlp {

        private String endpoint = "http://localhost:4318";

        private String transport = "http"; // http | grpc

        public void setEndpoint(String endpoint) {
            if (endpoint == null || endpoint.isBlank()) {
                throw new IllegalArgumentException("OTLP endpoint must not be empty");
            }
            this.endpoint = endpoint;
        }

    }
}