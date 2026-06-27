package com.omnixys.observability.tracing;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AdaptiveSampler implements Sampler {

    private final double baseRate;

    public AdaptiveSampler() {
        this(0.1);
    }

    public AdaptiveSampler(double baseRate) {
        this.baseRate = baseRate;
    }

    @Override
    public SamplingResult shouldSample(Context parentContext, String traceId, String spanName,
                                       SpanKind spanKind, Attributes attributes, List<LinkData> parentLinks) {
        if (traceId != null && traceId.endsWith("ff")) {
            return SamplingResult.recordAndSample();
        }
        if (ThreadLocalRandom.current().nextDouble() < baseRate) {
            return SamplingResult.recordAndSample();
        }
        return SamplingResult.drop();
    }

    @Override
    public String getDescription() {
        return "AdaptiveSampler{baseRate=" + baseRate + "}";
    }
}
