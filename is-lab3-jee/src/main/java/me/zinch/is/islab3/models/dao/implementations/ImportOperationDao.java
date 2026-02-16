package me.zinch.is.islab3.models.dao.implementations;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import me.zinch.is.islab3.cache.DatabaseFailureDetector;
import me.zinch.is.islab3.Config;
import me.zinch.is.islab3.cache.InfinispanL2CacheBridge;
import me.zinch.is.islab3.exceptions.StorageUnavailableException;
import me.zinch.is.islab3.models.entities.ImportOperation;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@ApplicationScoped
public class ImportOperationDao {
    @PersistenceContext(unitName = Config.UNIT_NAME)
    private EntityManager em;

    @Inject
    private DatabaseFailureDetector databaseFailureDetector;

    public Optional<ImportOperation> findById(Integer id) {
        return readThroughCache(
                "importOperation:findById:" + id,
                () -> Optional.ofNullable(em.find(ImportOperation.class, id))
        );
    }

    public ImportOperation create(ImportOperation operation) {
        em.persist(operation);
        em.flush();
        InfinispanL2CacheBridge.clearQueryCache();
        return operation;
    }

    public ImportOperation update(ImportOperation operation) {
        ImportOperation merged = em.merge(operation);
        InfinispanL2CacheBridge.clearQueryCache();
        return merged;
    }

    public List<ImportOperation> findAll() {
        return readThroughCache(
                "importOperation:findAll",
                () -> cacheQuery(em.createQuery(
                                "SELECT i FROM ImportOperation i ORDER BY i.id DESC",
                                ImportOperation.class
                        ))
                        .getResultList()
        );
    }

    public List<ImportOperation> findAllByUser(Integer userId) {
        return readThroughCache(
                "importOperation:findAllByUser:" + userId,
                () -> cacheQuery(em.createQuery(
                                "SELECT i FROM ImportOperation i WHERE i.user.id = :userId ORDER BY i.id DESC",
                                ImportOperation.class
                        ))
                        .setParameter("userId", userId)
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
                    "Primary database is temporarily unavailable and no cached import operations are available.",
                    ex
            );
        }
    }
}
