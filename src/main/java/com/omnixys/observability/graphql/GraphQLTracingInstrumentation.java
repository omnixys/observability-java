package com.omnixys.observability.graphql;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

public class GraphQLTracingInstrumentation extends SimpleInstrumentation {


    public InstrumentationContext<ExecutionResult> beginExecution(
            InstrumentationExecutionParameters parameters) {

        Tracer tracer = GlobalOpenTelemetry.getTracer("omnixys.graphql");

        Context parentContext = Context.current();

        // ✅ kompatibel mit ALLEN graphql-java Versionen
        String operationName = parameters.getOperation();

        if (operationName == null || operationName.isBlank()) {
            operationName = "anonymous";
        }

        String spanName = "GraphQL " + operationName;

        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.INTERNAL)
                .setParent(parentContext)
                .startSpan();

        return new InstrumentationContext<ExecutionResult>() {

            private Scope scope;

            @Override
            public void onDispatched() {
                scope = span.makeCurrent();
            }

            @Override
            public void onCompleted(ExecutionResult result, Throwable t) {
                try {
                    if (t != null) {
                        span.recordException(t);
                        span.setStatus(StatusCode.ERROR);
                    }
                } finally {
                    span.end();

                    if (scope != null) {
                        scope.close();
                    }
                }
            }
        };
    }
}