package com.omnixys.observability.api;

public record TraceContext(
        String traceId,
        String spanId
) {}