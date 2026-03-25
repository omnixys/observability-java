package com.omnixys.observability.annotation;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SpanAspect {

    @Around("@annotation(spanAnnotation)")
    public Object trace(
            ProceedingJoinPoint pjp,
            TraceSpan traceSpanAnnotation
    ) throws Throwable {
        Tracer tracer = GlobalOpenTelemetry.getTracer("omnixys.service");

        String name = traceSpanAnnotation.value().isEmpty()
                ? pjp.getSignature().getName()
                : traceSpanAnnotation.value();

        io.opentelemetry.api.trace.Span span = tracer.spanBuilder(name)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            return pjp.proceed();
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            span.end();
        }
    }
}