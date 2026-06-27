package com.omnixys.observability.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class SloMetricsService {

    private final AtomicLong total = new AtomicLong();
    private final AtomicLong errors = new AtomicLong();

    public void recordSuccess() {
        total.incrementAndGet();
    }

    public void recordError() {
        total.incrementAndGet();
        errors.incrementAndGet();
    }

    public double errorRate() {
        long t = total.get();
        return t == 0 ? 0.0 : (double) errors.get() / t;
    }

    public long total() {
        return total.get();
    }

    public long errors() {
        return errors.get();
    }
}
