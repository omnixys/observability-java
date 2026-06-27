package com.omnixys.observability.context;

import io.opentelemetry.context.Context;

@Deprecated(forRemoval = true)
public interface ITraceContext {

    String traceId();

    String spanId();

    boolean sampled();

    boolean isValid();

    Context otelContext();
}