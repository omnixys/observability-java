package com.omnixys.observability.otel;

import com.omnixys.observability.api.*;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

public class OpenTelemetryTracePropagation implements TracePropagation<Object> {

    private final Tracer tracer =
            GlobalOpenTelemetry.getTracer("omnixys");

    @Override
    public void inject(Object carrier) {
        GlobalOpenTelemetry.get()
                .getPropagators()
                .getTextMapPropagator()
                .inject(Context.current(), carrier, (c, key, value) -> {
                    if (c instanceof HeaderSetter setter) {
                        setter.set(key, value);
                    }
                });
    }

    @Override
    public TraceContext currentContext() {
        Span span = Span.current();
        SpanContext ctx = span.getSpanContext();

        if (!ctx.isValid()) {
            return null;
        }

        return new TraceContext(
                ctx.getTraceId(),
                ctx.getSpanId()
        );
    }

    @Override
    public TraceContextSnapshot capture() {
        Context ctx = Context.current();

        return () -> {
            Scope scope = ctx.makeCurrent();
            return scope::close;
        };
    }

    @Override
    public <T> T runWithSpan(String name, TraceSpanKind kind, TraceSupplier<T> fn) {
        Context parent = Context.current();

        Span span = tracer.spanBuilder(name)
                .setSpanKind(map(kind))
                .setParent(parent)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            T result = fn.get();
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

    private SpanKind map(TraceSpanKind kind) {
        return switch (kind) {
            case PRODUCER -> SpanKind.PRODUCER;
            case CONSUMER -> SpanKind.CONSUMER;
            case CLIENT -> SpanKind.CLIENT;
            case SERVER -> SpanKind.SERVER;
            default -> SpanKind.INTERNAL;
        };
    }
}