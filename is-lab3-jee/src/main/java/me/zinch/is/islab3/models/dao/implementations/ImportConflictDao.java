package me.zinch.is.islab3.models.dao.implementations;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import me.zinch.is.islab3.server.cache.DatabaseFailureDetector;
import me.zinch.is.islab3.Config;
import me.zinch.is.islab3.server.cache.InfinispanL2CacheBridge;
import me.zinch.is.islab3.exceptions.StorageUnavailableException;
import me.zinch.is.islab3.models.entities.ImportConflict;
import me.zinch.is.islab3.models.entities.ImportConflictResolution;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@ApplicationScoped
public class ImportConflictDao {
    @PersistenceContext(unitName = Config.UNIT_NAME)
    private EntityManager em;

    @Inject
    private DatabaseFailureDetector databaseFailureDetector;

    public Optional<ImportConflict> findById(Integer id) {
        return readThroughCache(
                "importConflict:findById:" + id,
                () -> Optional.ofNullable(em.find(ImportConflict.class, id))
        );
    }

    public ImportConflict create(ImportConflict conflict) {
        em.persist(conflict);
        em.flush();
        InfinispanL2CacheBridge.clearQueryCache();
        return conflict;
    }

    public ImportConflict update(ImportConflict conflict) {
        ImportConflict merged = em.merge(conflict);
        InfinispanL2CacheBridge.clearQueryCache();
        return merged;
    }

    public List<ImportConflict> findByOperation(Integer operationId) {
        return readThroughCache(
                "importConflict:findByOperation:" + operationId,
                () -> cacheQuery(em.createQuery(
                                "SELECT c FROM ImportConflict c WHERE c.operation.id = :operationId ORDER BY c.id ASC",
                                ImportConflict.class
                        ))
                        .setParameter("operationId", operationId)
                        .getResultList()
        );
    }

    public List<ImportConflict> findUnresolvedByOperation(Integer operationId) {
        return readThroughCache(
                "importConflict:findUnresolvedByOperation:" + operationId,
                () -> cacheQuery(em.createQuery(
                                "SELECT c FROM ImportConflict c WHERE c.operation.id = :operationId AND c.resolution = :resolution ORDER BY c.id ASC",
                                ImportConflict.class
                        ))
                        .setParameter("operationId", operationId)
                        .setParameter("resolution", ImportConflictResolution.UNRESOLVED)
                        .getResultList()
        );
    }

    private <Q extends Query> Q cacheQuery(Q query) {
        query.setHint("eclipselink.query-results-cache", "true");
        query.setHint("eclipselink.maintain-cache", "true");
        return query;
    }

    private <R> R readThroughCache(String cacheKey, Supplier<R> dbQuery) {
        if (databaseFailureDetector.isDatabaseLikelyDown()) {
            Object cached = InfinispanL2CacheBridge.getQueryResult(cacheKey);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                R restored = (R) cached;
                return restored;
            }
        }
        try {
            R result = dbQuery.get();
            databaseFailureDetector.markSuccess();
            InfinispanL2CacheBridge.putQueryResult(cacheKey, result);
            return result;
        } catch (RuntimeException ex) {
            if (!databaseFailureDetector.isDbCommunicationFailure(ex)) {
                throw ex;
            }
            databaseFailureDetector.markFailure(ex);
            Object cached = InfinispanL2CacheBridge.getQueryResult(cacheKey);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                R restored = (R) cached;
                return restored;
            }
            throw new StorageUnavailableException(
                    "Primary database is temporarily unavailable and no cached import conflicts are available.",
                    ex
            );
        }
    }
}
