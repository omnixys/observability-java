package com.omnixys.observability.api;

@Deprecated(forRemoval = true)
public record TraceContext(
        String traceId,
        String spanId
) {
    public com.omnixys.commons.model.TraceContext toCommons() {
        return new com.omnixys.commons.model.TraceContext(traceId, spanId, null, null);
    }
}