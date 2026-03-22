package com.omnixys.observability.context;

import io.opentelemetry.context.Context;

/**
 * Immutable trace context representation.
 */
public record TraceContextDTO(
        String traceId,
        String spanId,
        boolean sampled,
        Context context
) implements ITraceContext {

    @Override
    public boolean isValid() {
        return traceId != null && spanId != null;
    }

    @Override
    public Context otelContext() {
        return context;
    }
}