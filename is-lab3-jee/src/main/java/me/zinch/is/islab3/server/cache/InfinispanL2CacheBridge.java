package me.zinch.is.islab3.server.cache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class InfinispanL2CacheBridge {
    private static final String CACHE_NAME = "eclipselink-l2-cache";
    private static final String QUERY_CACHE_NAME = "query-fallback-cache";
    private static final CachingProvider PROVIDER = Caching.getCachingProvider("org.infinispan.jcache.embedded.JCachingProvider");
    private static final CacheManager CACHE_MANAGER = PROVIDER.getCacheManager();
    private static final Cache<String, Object> CACHE = initOrGetCache(CACHE_NAME);
    private static final Cache<String, Object> QUERY_CACHE = initOrGetCache(QUERY_CACHE_NAME);

    private static final AtomicLong HIT_COUNT = new AtomicLong(0);
    private static final AtomicLong MISS_COUNT = new AtomicLong(0);
    private static final AtomicLong PUT_COUNT = new AtomicLong(0);
    // Per-thread counters are used for interceptor deltas so concurrent requests
    // do not pollute each other's "this invocation" stats.
    private static final ThreadLocal<long[]> THREAD_COUNTS = ThreadLocal.withInitial(() -> new long[3]);

    private InfinispanL2CacheBridge() {
    }

    public static void warmUp() {
        CACHE.get("__warmup__");
        QUERY_CACHE.get("__warmup__");
    }

    private static Cache<String, Object> initOrGetCache(String cacheName) {
        Cache<String, Object> existing = CACHE_MANAGER.getCache(cacheName, String.class, Object.class);
        if (existing != null) {
            return existing;
        }
        MutableConfiguration<String, Object> cfg = new MutableConfiguration<>();
        cfg.setStoreByValue(false);
        cfg.setStatisticsEnabled(true);
        return CACHE_MANAGER.createCache(cacheName, cfg);
    }

    public static Object get(String entityName, Object primaryKey) {
        return CACHE.get(toKey(entityName, primaryKey));
    }

    public static void put(String entityName, Object primaryKey, Object value) {
        CACHE.put(toKey(entityName, primaryKey), value);
    }

    public static void remove(String entityName, Object primaryKey) {
        CACHE.remove(toKey(entityName, primaryKey));
    }

    public static void incHit() {
        HIT_COUNT.incrementAndGet();
        THREAD_COUNTS.get()[0]++;
    }

    public static void incMiss() {
        MISS_COUNT.incrementAndGet();
        THREAD_COUNTS.get()[1]++;
    }

    public static void incPut() {
        PUT_COUNT.incrementAndGet();
        THREAD_COUNTS.get()[2]++;
    }

    public static Object getQueryResult(String key) {
        return QUERY_CACHE.get(key);
    }

    public static void putQueryResult(String key, Object value) {
        QUERY_CACHE.put(key, value);
    }

    public static void clearQueryCache() {
        QUERY_CACHE.clear();
    }

    public static long getHitCount() {
        return HIT_COUNT.get();
    }

    public static long getMissCount() {
        return MISS_COUNT.get();
    }

    public static long getPutCount() {
        return PUT_COUNT.get();
    }

    public static long getThreadHitCount() {
        return THREAD_COUNTS.get()[0];
    }

    public static long getThreadMissCount() {
        return THREAD_COUNTS.get()[1];
    }

    public static long getThreadPutCount() {
        return THREAD_COUNTS.get()[2];
    }

    private static String toKey(String entityName, Object primaryKey) {
        String type = entityName == null ? "unknown" : entityName;
        Object key = primaryKey == null ? "null" : primaryKey;
        return type + ":" + Objects.toString(key);
    }
}
