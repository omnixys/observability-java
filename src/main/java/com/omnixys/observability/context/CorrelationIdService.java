package com.omnixys.observability.context;

import com.omnixys.context.ContextAccessor;

import java.util.UUID;

public class CorrelationIdService {

    public String get() {
        var ctx = ContextAccessor.get();
        return ctx != null ? ctx.correlationId() : null;
    }

    public String generate() {
        return UUID.randomUUID().toString();
    }
}
