package com.omnixys.observability.http;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Scope;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class HttpTracingFilter implements Filter {

    private Tracer getTracer() {
        return GlobalOpenTelemetry.getTracer("omnixys.http");
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        Tracer tracer = getTracer();

        String spanName = httpRequest.getMethod() + " " + httpRequest.getRequestURI();

        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("http.method", httpRequest.getMethod())
                .setAttribute("http.route", httpRequest.getRequestURI())
                .setAttribute("http.target", httpRequest.getRequestURI())
                .setAttribute("http.scheme", httpRequest.getScheme())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {

            chain.doFilter(request, response);

            span.setAttribute("http.status_code", httpResponse.getStatus());

        } catch (Exception e) {

            span.recordException(e);
            span.setAttribute("error", true);
            throw e;

        } finally {
            span.end();
        }
    }
}