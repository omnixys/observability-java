package com.omnixys.observability.context;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;

public final class TraceContextExtractor {

    private TraceContextExtractor() {}

    public static ITraceContext current() {
        return fromContext(Context.current());
    }

    public static ITraceContext fromContext(Context context) {

        Span span = Span.fromContext(context);
        SpanContext spanContext = span.getSpanContext();

        if (!spanContext.isValid()) {
            return new TraceContextDTO(null, null, false, context);
        }

        return new TraceContextDTO(
                spanContext.getTraceId(),
                spanContext.getSpanId(),
                spanContext.isSampled(),
                context
        );
    }

    public static String getTraceId() {
        return current().traceId();
    }

    public static String getSpanId() {
        return current().spanId();
    }
}