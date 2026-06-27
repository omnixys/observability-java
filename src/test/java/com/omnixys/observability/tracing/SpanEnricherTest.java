package com.omnixys.observability.tracing;

import com.omnixys.context.*;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpanEnricherTest {

    @Mock Span span;

    @AfterEach
    void tearDown() {
        ContextAccessor.clear();
    }

    @Test
    void shouldEnrichSpanWithAllContextData() {
        var trace = new TraceMetadata("trace-1", "span-1");
        var tenant = new TenantContext("tenant-1", "header", true);
        var principal = new PrincipalContext("subject-1", "actor-1", "user-1", "tenant-1", java.util.List.of(), "session-1", "strong", 1000L);
        var client = new ClientMetadata("10.0.0.1", "agent", null, null, null, null, null, null, null);
        var transport = new TransportMetadata("http", null, null, null, null, null, null, null, null, null, null, null, null);
        var snapshot = new ContextSnapshot("req-1", "corr-1", System.currentTimeMillis(), tenant, principal, client, transport, trace);
        ContextAccessor.set(snapshot);

        SpanEnricher.enrich(span);

        var captor = ArgumentCaptor.forClass(Attributes.class);
        verify(span).setAllAttributes(captor.capture());
        var attrs = captor.getValue();

        assertEquals("trace-1", attrs.get(AttributeKey.stringKey("trace.id")));
        assertEquals("span-1", attrs.get(AttributeKey.stringKey("span.id")));
        assertEquals("req-1", attrs.get(AttributeKey.stringKey("request.id")));
        assertEquals("corr-1", attrs.get(AttributeKey.stringKey("correlation.id")));
        assertEquals("tenant-1", attrs.get(AttributeKey.stringKey("tenant.id")));
        assertEquals("actor-1", attrs.get(AttributeKey.stringKey("actor.id")));
        assertEquals("user-1", attrs.get(AttributeKey.stringKey("user.id")));
        assertEquals("10.0.0.1", attrs.get(AttributeKey.stringKey("client.address")));
        assertEquals("http", attrs.get(AttributeKey.stringKey("transport.type")));
    }

    @Test
    void shouldNotFailWhenContextIsNull() {
        ContextAccessor.clear();

        SpanEnricher.enrich(span);

        verifyNoInteractions(span);
    }

    @Test
    void shouldHandleMissingFieldsGracefully() {
        var client = new ClientMetadata(null, null, null, null, null, null, null, null, null);
        var transport = new TransportMetadata(null, null, null, null, null, null, null, null, null, null, null, null, null);
        var snapshot = new ContextSnapshot("req-1", "corr-1", System.currentTimeMillis(), null, null, client, transport, null);
        ContextAccessor.set(snapshot);

        SpanEnricher.enrich(span);

        var captor = ArgumentCaptor.forClass(Attributes.class);
        verify(span).setAllAttributes(captor.capture());
        var attrs = captor.getValue();

        assertEquals("req-1", attrs.get(AttributeKey.stringKey("request.id")));
        assertEquals("corr-1", attrs.get(AttributeKey.stringKey("correlation.id")));
        assertNull(attrs.get(AttributeKey.stringKey("trace.id")));
        assertNull(attrs.get(AttributeKey.stringKey("tenant.id")));
        assertNull(attrs.get(AttributeKey.stringKey("actor.id")));
    }

    @Test
    void shouldEnrichSpanWithPartialPrincipal() {
        var client = new ClientMetadata(null, null, null, null, null, null, null, null, null);
        var transport = new TransportMetadata(null, null, null, null, null, null, null, null, null, null, null, null, null);
        var principal = new PrincipalContext("subject-1", null, "user-1", null, java.util.List.of(), null, null, null);
        var snapshot = new ContextSnapshot("req-1", "corr-1", System.currentTimeMillis(), null, principal, client, transport, null);
        ContextAccessor.set(snapshot);

        SpanEnricher.enrich(span);

        var captor = ArgumentCaptor.forClass(Attributes.class);
        verify(span).setAllAttributes(captor.capture());
        var attrs = captor.getValue();

        assertNull(attrs.get(AttributeKey.stringKey("actor.id")));
        assertEquals("user-1", attrs.get(AttributeKey.stringKey("user.id")));
    }
}
