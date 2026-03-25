package com.omnixys.observability.http;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

public class HttpTracingFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 🔥 LAZY: erst hier holen!
        Tracer tracer = GlobalOpenTelemetry.getTracer("omnixys.http");

        Context extractedContext = GlobalOpenTelemetry.get()
                .getPropagators()
                .getTextMapPropagator()
                .extract(
                        Context.current(),
                        req,
                        new TextMapGetter<>() {
                            @Override
                            public Iterable<String> keys(HttpServletRequest carrier) {
                                return Collections.list(carrier.getHeaderNames());
                            }

                            @Override
                            public String get(HttpServletRequest carrier, String key) {
                                return carrier.getHeader(key);
                            }
                        }
                );

        String spanName = req.getMethod() + " " + req.getRequestURI();

        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.SERVER)
                .setParent(extractedContext)
                .startSpan();

        try (Scope scope = extractedContext.makeCurrent();
             Scope scope2 = span.makeCurrent()) {

            chain.doFilter(request, response);
            span.setAttribute("http.status_code", res.getStatus());

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            span.end();
        }
    }
}