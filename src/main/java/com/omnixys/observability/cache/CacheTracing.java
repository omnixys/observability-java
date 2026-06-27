package com.omnixys.observability.cache;

import com.omnixys.observability.tracing.SpanEnricher;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import java.util.concurrent.Callable;

public class CacheTracing {

    private final Tracer tracer;

    public CacheTracing(Tracer tracer) {
        this.tracer = tracer;
    }

    public <T> T tracePublish(String channel, Callable<T> fn) {
        Span span = tracer.spanBuilder("valkey.publish " + channel)
                .setSpanKind(SpanKind.PRODUCER)
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            SpanEnricher.enrich(span);
            T result = fn.call();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw new RuntimeException(e);
        } finally {
            span.end();
        }
    }

    public <T> T traceSubscribe(String channel, Callable<T> fn) {
        Span span = tracer.spanBuilder("valkey.subscribe " + channel)
                .setSpanKind(SpanKind.CONSUMER)
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            SpanEnricher.enrich(span);
            T result = fn.call();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw new RuntimeException(e);
        } finally {
            span.end();
        }
    }
}
