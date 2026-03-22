package com.omnixys.observability.propagation;

import com.omnixys.observability.context.ITraceContext;
import com.omnixys.observability.context.TraceContextExtractor;
import io.opentelemetry.context.Context;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class W3CTraceContextPropagator implements ContextPropagator {

    @Override
    public void inject(ITraceContext context, HeaderCarrier carrier) {

        TextMapSetter<HeaderCarrier> setter =
                (c, key, value) -> c.set(key, value);

        GlobalOpenTelemetry.getPropagators()
                .getTextMapPropagator()
                .inject(context.otelContext(), carrier, setter);
    }

    @Override
    public ITraceContext extract(HeaderCarrier carrier) {

        TextMapGetter<HeaderCarrier> getter = new TextMapGetter<>() {
            @Override
            public Iterable<String> keys(HeaderCarrier c) {
                return java.util.List.of(
                        TraceHeaders.TRACE_PARENT,
                        TraceHeaders.TRACE_STATE
                );
            }

            @Override
            public String get(HeaderCarrier c, String key) {
                return c.get(key);
            }
        };

        Context extracted = GlobalOpenTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.root(), carrier, getter);

        return TraceContextExtractor.fromContext(extracted);
    }
}