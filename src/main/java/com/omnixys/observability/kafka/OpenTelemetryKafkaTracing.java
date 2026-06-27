package com.omnixys.observability.kafka;

import com.omnixys.observability.api.HeaderSetter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;

import java.util.function.Supplier;

public class OpenTelemetryKafkaTracing implements KafkaTracing {

    private final Tracer tracer;

    public OpenTelemetryKafkaTracing(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void inject(Object carrier) {
        if (carrier == null) return;
        GlobalOpenTelemetry.get()
                .getPropagators()
                .getTextMapPropagator()
                .inject(Context.current(), carrier, (TextMapSetter<Object>) (c, key, value) -> {
                    if (c instanceof HeaderSetter setter) {
                        setter.set(key, value);
                    }
                });
    }

    @Override
    public <T> T produce(String topic, Supplier<T> fn) {
        Span span = tracer.spanBuilder("kafka.produce " + topic)
                .setSpanKind(SpanKind.PRODUCER)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("messaging.system", "kafka");
            span.setAttribute("messaging.destination", topic);
            span.setAttribute("messaging.operation", "produce");

            T result = fn.get();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw new RuntimeException("Kafka produce failed for topic: " + topic, e);
        } finally {
            span.end();
        }
    }
}
