package me.zinch.is.islab2.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ImportPayloadCache {
    private static final Logger LOGGER = Logger.getLogger(ImportPayloadCache.class.getName());
    private static final long TTL_MS = 10L * 60L * 1000L;

    private final Map<Integer, CacheEntry> cache = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;
    private ImportService importService;

    @Inject
    public void setImportService(ImportService importService) {
        this.importService = importService;
    }

    @PostConstruct
    public void init() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "import-payload-cleaner");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.MINUTES);
    }

    public void put(Integer operationId, String payload) {
        if (operationId == null || payload == null) {
            return;
        }
        long expiresAt = System.currentTimeMillis() + TTL_MS;
        cache.put(operationId, new CacheEntry(payload, expiresAt));
    }

    public Optional<String> get(Integer operationId) {
        CacheEntry entry = cache.get(operationId);
        if (entry == null) {
            return Optional.empty();
        }
        if (System.currentTimeMillis() > entry.expiresAt()) {
            cache.remove(operationId);
            return Optional.empty();
        }
        return Optional.of(entry.payload());
    }

    public void remove(Integer operationId) {
        cache.remove(operationId);
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> {
            if (now <= entry.getValue().expiresAt()) {
                return false;
            }
            Integer operationId = entry.getKey();
            if (importService != null) {
                try {
                    importService.failExpired(operationId);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to mark import expired for operation id=" + operationId, e);
                }
            }
            return true;
        });
    }

    @PreDestroy
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private record CacheEntry(String payload, long expiresAt) {
    }
}
