package me.zinch.is.islab3.server.cache;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class L2CacheStatsService {
    private final AtomicBoolean loggingEnabled = new AtomicBoolean(false);

    public boolean isLoggingEnabled() {
        return loggingEnabled.get();
    }

    public void setLoggingEnabled(boolean enabled) {
        loggingEnabled.set(enabled);
    }

    public L2CacheStatsSnapshot snapshot() {
        return new L2CacheStatsSnapshot(
                isLoggingEnabled(),
                InfinispanL2CacheBridge.getHitCount(),
                InfinispanL2CacheBridge.getMissCount(),
                InfinispanL2CacheBridge.getPutCount()
        );
    }

    public L2CacheStatsSnapshot threadSnapshot() {
        return new L2CacheStatsSnapshot(
                isLoggingEnabled(),
                InfinispanL2CacheBridge.getThreadHitCount(),
                InfinispanL2CacheBridge.getThreadMissCount(),
                InfinispanL2CacheBridge.getThreadPutCount()
        );
    }
}
