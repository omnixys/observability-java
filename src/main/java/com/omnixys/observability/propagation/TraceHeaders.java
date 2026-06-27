package com.omnixys.observability.propagation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Deprecated(forRemoval = true)
public final class TraceHeaders {

    public static final String TRACE_PARENT = "traceparent";
    public static final String TRACE_STATE = "tracestate";
}
