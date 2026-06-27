package com.omnixys.observability.tracing;

import com.omnixys.context.ContextAccessor;
import com.omnixys.context.ContextSnapshot;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;

public final class SpanEnricher {

    private SpanEnricher() {}

    public static void enrich(Span span) {
        ContextSnapshot ctx = ContextAccessor.get();
        if (ctx == null) return;

        AttributesBuilder attrs = Attributes.builder();

        if (ctx.trace() != null) {
            attrs.put("trace.id", ctx.trace().traceId());
            attrs.put("span.id", ctx.trace().spanId());
        }
        if (ctx.requestId() != null) attrs.put("request.id", ctx.requestId());
        if (ctx.correlationId() != null) attrs.put("correlation.id", ctx.correlationId());

        if (ctx.tenant() != null) {
            if (ctx.tenant().tenantId() != null) attrs.put("tenant.id", ctx.tenant().tenantId());
        }
        if (ctx.principal() != null) {
            if (ctx.principal().actorId() != null) attrs.put("actor.id", ctx.principal().actorId());
            if (ctx.principal().userId() != null) attrs.put("user.id", ctx.principal().userId());
        }
        if (ctx.client() != null) {
            if (ctx.client().ip() != null) attrs.put("client.address", ctx.client().ip());
        }
        if (ctx.transport() != null) {
            if (ctx.transport().type() != null) attrs.put("transport.type", ctx.transport().type());
        }

        span.setAllAttributes(attrs.build());
    }
}
