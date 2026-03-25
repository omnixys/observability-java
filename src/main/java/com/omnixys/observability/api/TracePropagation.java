package com.omnixys.observability.api;

public interface TracePropagation<CARRIER> {

    void inject(CARRIER carrier);

    TraceContext currentContext();

    TraceContextSnapshot capture();

    <T> T runWithSpan(String name, TraceSpanKind kind, TraceSupplier<T> fn);
}