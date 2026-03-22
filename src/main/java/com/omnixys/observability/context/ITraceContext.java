package com.omnixys.observability.context;

import io.opentelemetry.context.Context;

public interface ITraceContext {

    String traceId();

    String spanId();

    boolean sampled();

    boolean isValid();

    Context otelContext();
}