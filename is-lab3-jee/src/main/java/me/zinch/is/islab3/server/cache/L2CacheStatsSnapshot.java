package me.zinch.is.islab3.server.cache;

public class L2CacheStatsSnapshot {
    private boolean loggingEnabled;
    private long hitCount;
    private long missCount;
    private long putCount;

    public L2CacheStatsSnapshot() {
    }

    public L2CacheStatsSnapshot(boolean loggingEnabled, long hitCount, long missCount, long putCount) {
        this.loggingEnabled = loggingEnabled;
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.putCount = putCount;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public long getHitCount() {
        return hitCount;
    }

    public void setHitCount(long hitCount) {
        this.hitCount = hitCount;
    }

    public long getMissCount() {
        return missCount;
    }

    public void setMissCount(long missCount) {
        this.missCount = missCount;
    }

    public long getPutCount() {
        return putCount;
    }

    public void setPutCount(long putCount) {
        this.putCount = putCount;
    }
}
