package com.omnixys.observability.cache;

import java.util.concurrent.atomic.AtomicLong;

public class CacheMetricsRecorder {

    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong writes = new AtomicLong();
    private final AtomicLong deletes = new AtomicLong();
    private final AtomicLong errors = new AtomicLong();

    public void recordHit() { hits.incrementAndGet(); }
    public void recordMiss() { misses.incrementAndGet(); }
    public void recordWrite() { writes.incrementAndGet(); }
    public void recordDelete() { deletes.incrementAndGet(); }
    public void recordError() { errors.incrementAndGet(); }

    public long getHits() { return hits.get(); }
    public long getMisses() { return misses.get(); }
    public long getWrites() { return writes.get(); }
    public long getDeletes() { return deletes.get(); }
    public long getErrors() { return errors.get(); }

    public CacheMetricsSnapshot snapshot() {
        long h = hits.get();
        long m = misses.get();
        long total = h + m;
        return new CacheMetricsSnapshot(h, m, writes.get(), deletes.get(), errors.get(),
                total == 0 ? 0.0 : (double) h / total);
    }

    public record CacheMetricsSnapshot(
            long hits,
            long misses,
            long writes,
            long deletes,
            long errors,
            double hitRate
    ) {}
}
