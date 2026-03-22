package com.omnixys.observability.tracing;

import com.omnixys.observability.properties.ObservabilityProperties;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import java.time.Duration;
import java.util.Map;

/**
 * Factory for building OpenTelemetry SDK instance.
 * This is the central entrypoint for Omnixys tracing.
 */
public final class OpenTelemetryFactory {

    private OpenTelemetryFactory() {}

    public static OpenTelemetry create(ObservabilityProperties properties) {
        System.out.println("🔥 OpenTelemetry initialized");

        // ---------------------------------------------------------------------
        // Resource (service metadata)
        // ---------------------------------------------------------------------
        AttributesBuilder attributesBuilder = Attributes.builder()
                .put("service.name", properties.getServiceName());

        if (properties.getResourceAttributes() != null) {
            for (Map.Entry<String, String> entry : properties.getResourceAttributes().entrySet()) {
                attributesBuilder.put(entry.getKey(), entry.getValue());
            }
        }

        Resource resource = Resource.getDefault()
                .merge(Resource.create(attributesBuilder.build()));

        // ---------------------------------------------------------------------
        // Exporter (HTTP or gRPC)
        // ---------------------------------------------------------------------
        BatchSpanProcessor spanProcessor;

        if ("grpc".equalsIgnoreCase(properties.getOtlp().getTransport())) {

            OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder()
                    .setEndpoint(properties.getOtlp().getEndpoint())
                    .setTimeout(Duration.ofSeconds(10))
                    .build();

            spanProcessor = BatchSpanProcessor.builder(exporter).build();

        } else {

            OtlpHttpSpanExporter exporter = OtlpHttpSpanExporter.builder()
                    .setEndpoint(properties.getOtlp().getEndpoint())
                    .setTimeout(Duration.ofSeconds(10))
                    .build();


            spanProcessor = BatchSpanProcessor.builder(exporter)
                    .setScheduleDelay(Duration.ofMillis(100))
                    .build();
        }

        // ---------------------------------------------------------------------
        // Sampler
        // ---------------------------------------------------------------------
        Sampler sampler = Sampler.traceIdRatioBased(
                properties.getTracing().getSamplingProbability()
        );

        // ---------------------------------------------------------------------
        // Tracer Provider
        // ---------------------------------------------------------------------
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setSampler(sampler)
                .setResource(resource)
                .addSpanProcessor(spanProcessor)
                .build();


        // ---------------------------------------------------------------------
        // Graceful shutdown
        // ---------------------------------------------------------------------
        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

//        return sdk;
        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(
                        ContextPropagators.create(
                                W3CTraceContextPropagator.getInstance()
                        )
                )
                .buildAndRegisterGlobal();
    }
}