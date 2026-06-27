package com.omnixys.observability.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimitMetrics {

    private final ConcurrentHashMap<String, AtomicLong> hits = new ConcurrentHashMap<>();

    public void hit(String key) {
        hits.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();
    }

    public long get(String key) {
        AtomicLong counter = hits.get(key);
        return counter != null ? counter.get() : 0;
    }

    public Map<String, Long> snapshot() {
        return hits.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey, e -> e.getValue().get()));
    }

    public void reset() {
        hits.clear();
    }
}
