package me.zinch.is.islab3.models.dao.implementations;

import me.zinch.is.islab3.Config;
import me.zinch.is.islab3.exceptions.ConstraintException;
import me.zinch.is.islab3.exceptions.FieldValueConvertException;
import me.zinch.is.islab3.exceptions.StorageUnavailableException;
import me.zinch.is.islab3.models.dao.interfaces.IConverter;
import me.zinch.is.islab3.models.dao.interfaces.IDao;
import me.zinch.is.islab3.models.dao.support.DaoExceptionSupport;
import me.zinch.is.islab3.models.dao.support.DaoQueryCacheFallbackSupport;
import me.zinch.is.islab3.models.fields.EntityField;
import me.zinch.is.islab3.models.fields.Filter;
import me.zinch.is.islab3.cache.DatabaseFailureDetector;
import me.zinch.is.islab3.cache.InfinispanL2CacheBridge;
import me.zinch.is.islab3.cache.LogL2CacheStats;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@LogL2CacheStats
public abstract class AbstractDao<T, F extends EntityField> implements IDao<T, F> {
    @PersistenceContext(unitName = Config.UNIT_NAME)
    protected EntityManager em;

    @Inject
    protected DatabaseFailureDetector databaseFailureDetector;

    private final Class<T> entityClass;
    private IConverter converter;

    protected AbstractDao(Class<T> entityClass, IConverter converter) {
        this.entityClass = entityClass;
        this.converter = converter;
    }

    protected AbstractDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Optional<T> findById(Integer id) {
        if (databaseFailureDetector.isDatabaseLikelyDown()) {
            Object cached = InfinispanL2CacheBridge.get(entityClass.getName(), id);
            if (entityClass.isInstance(cached)) {
                return Optional.of(entityClass.cast(cached));
            }
        }
        try {
            Optional<T> result = Optional.ofNullable(em.find(entityClass, id));
            databaseFailureDetector.markSuccess();
            return result;
        } catch (PersistenceException e) {
            if (!databaseFailureDetector.isDbCommunicationFailure(e)) {
                throw e;
            }
            databaseFailureDetector.markFailure(e);
            Object cached = InfinispanL2CacheBridge.get(entityClass.getName(), id);
            if (entityClass.isInstance(cached)) {
                return Optional.of(entityClass.cast(cached));
            }
            throw unavailableReadException("findById", e);
        }
    }

    @Override
    public Optional<T> findByIdForUpdate(Integer id) {
        return Optional.ofNullable(em.find(entityClass, id, LockModeType.PESSIMISTIC_WRITE));
    }

    @Override
    public List<T> findAll() {
        return readThroughCache(
                "findAll:" + entityClass.getName(),
                () -> cacheQuery(em.createQuery(String.format("SELECT c FROM %s c", entityClass.getSimpleName()), entityClass))
                        .getResultList()
        );
    }

    @Override
    public List<T> findAllPaged(Integer page, Integer pageSize, Filter<F> filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();
        List<Order> orders = new ArrayList<>();

        applyPredicate(predicates, filter, cb, root);
        applyOrder(orders, filter, cb, root);

        cq.select(root)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(orders);

        TypedQuery<T> q = cacheQuery(em.createQuery(cq));
        q.setMaxResults(pageSize);
        q.setFirstResult(page * pageSize);
        String cacheKey = "findAllPaged:" + entityClass.getName() + ":" + page + ":" + pageSize + ":" + filterKey(filter);
        if (databaseFailureDetector.isDatabaseLikelyDown()) {
            Object cached = InfinispanL2CacheBridge.getQueryResult(cacheKey);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                List<T> restored = (List<T>) cached;
                return restored;
            }
            List<T> fromAll = fallbackPageFromFindAllCache(page, pageSize, filter);
            if (fromAll != null) {
                return fromAll;
            }
        }
        try {
            List<T> result = q.getResultList();
            databaseFailureDetector.markSuccess();
            InfinispanL2CacheBridge.putQueryResult(cacheKey, result);
            return result;
        } catch (PersistenceException e) {
            if (DaoExceptionSupport.isFieldConversionError(e, filter)) {
                throw new FieldValueConvertException(
                    String.format("Не удалось сконвертировать значение %s для поля %s", filter.getValue(), filter.getField().getValue())
                );
            }
            if (databaseFailureDetector.isDbCommunicationFailure(e)) {
                databaseFailureDetector.markFailure(e);
                Object cached = InfinispanL2CacheBridge.getQueryResult(cacheKey);
                if (cached != null) {
                    @SuppressWarnings("unchecked")
                    List<T> restored = (List<T>) cached;
                    return restored;
                }
                List<T> fromAll = fallbackPageFromFindAllCache(page, pageSize, filter);
                if (fromAll != null) {
                    return fromAll;
                }
                throw unavailableReadException("findAllPaged", e);
            }
            throw e;
        }
    }

    @Override
    public Long count() {
        return readThroughCache(
                "count:" + entityClass.getName(),
                () -> cacheQuery(em.createQuery(String.format("SELECT COUNT(c) FROM %s c", entityClass.getSimpleName()), Long.class))
                        .getSingleResult()
        );
    }

    @Override
    public Long countPaged(Filter<F> filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();

        applyPredicate(predicates, filter, cb, root);

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        String cacheKey = "countPaged:" + entityClass.getName() + ":" + filterKey(filter);
        if (databaseFailureDetector.isDatabaseLikelyDown()) {
            Object cached = InfinispanL2CacheBridge.getQueryResult(cacheKey);
            if (cached instanceof Long restored) {
                return restored;
            }
            Long fromFindAll = fallbackCountFromFindAllCache(filter);
            if (fromFindAll != null) {
                return fromFindAll;
            }
        }
        try {
            Long result = cacheQuery(em.createQuery(cq)).getSingleResult();
            databaseFailureDetector.markSuccess();
            InfinispanL2CacheBridge.putQueryResult(cacheKey, result);
            return result;
        } catch (PersistenceException e) {
            if (DaoExceptionSupport.isFieldConversionError(e, filter)) {
                throw new FieldValueConvertException(
                        String.format("Не удалось сконвертировать значение %s для поля %s", filter.getValue(), filter.getField().getValue())
                );
            }
            if (databaseFailureDetector.isDbCommunicationFailure(e)) {
                databaseFailureDetector.markFailure(e);
                Object cached = InfinispanL2CacheBridge.getQueryResult(cacheKey);
                if (cached instanceof Long restored) {
                    return restored;
                }
                Long fromFindAll = fallbackCountFromFindAllCache(filter);
                if (fromFindAll != null) {
                    return fromFindAll;
                }
                throw unavailableReadException("countPaged", e);
            }
            throw e;
        }
    }

    @Override
    public T create(T entity) {
        try {
            em.persist(entity);
            em.flush();
            InfinispanL2CacheBridge.clearQueryCache();
            return entity;
        } catch (ConstraintViolationException e) {
            throw new ConstraintException(String.format("Ошибка валидации значения(ий). %s", getConstraintMessage(e)));
        } catch (PersistenceException e) {
            throw DaoExceptionSupport.mapPersistenceException(e, databaseFailureDetector);
        }
    }

    @Override
    public T update(T entity) {
        try {
            T merged = em.merge(entity);
            em.flush();
            InfinispanL2CacheBridge.clearQueryCache();
            return merged;
        } catch (PersistenceException e) {
            throw DaoExceptionSupport.mapPersistenceException(e, databaseFailureDetector);
        }
    }

    @Override
    public T delete(T entity) {
        try {
            em.remove(entity);
            em.flush();
            InfinispanL2CacheBridge.clearQueryCache();
            return entity;
        } catch (PersistenceException e) {
            throw DaoExceptionSupport.mapPersistenceException(e, databaseFailureDetector);
        }
    }

    protected void applyPredicate(List<Predicate> predicates, Filter<F> filter, CriteriaBuilder cb, Root<T> root) {
        if (filter == null || filter.getField() == null || filter.getValue() == null) {
            return;
        }

        String fieldName = filter.getField().getValue();
        Object preparedValue = converter == null
                ? filter.getValue()
                : converter.prepareField(filter.getField().getValue(), filter.getValue());

        if (filter.getField().isStringType()) {
            String pattern = String.format("%%%s%%", preparedValue.toString());
            predicates.add(cb.like(root.get(fieldName), pattern));
        } else {
            predicates.add(cb.equal(root.get(fieldName), preparedValue));
        }
    }

    protected void applyOrder(List<Order> orders, Filter<F> filter, CriteriaBuilder cb, Root<T> root) {
        if (filter.getField() != null && filter.getSortDirection() != null) {
            String fieldName = filter.getField().getValue();
            if (filter.getSortDirection().getValue().equalsIgnoreCase("ASC")) {
                orders.add(cb.asc(root.get(fieldName)));
            }
            if (filter.getSortDirection().getValue().equalsIgnoreCase("DESC")) {
                orders.add(cb.desc(root.get(fieldName)));
            }
        } else {
            orders.add(cb.asc(root.get("id")));
        }
    }

    protected String getConstraintMessage(ConstraintViolationException e) {
        return e.getConstraintViolations()
                .stream()
                .map(v -> String.format("%s: %s", v.getPropertyPath(), v.getMessage()))
                .collect(Collectors.joining(", "));
    }

    protected <Q extends Query> Q cacheQuery(Q query) {
        query.setHint("eclipselink.query-results-cache", "true");
        query.setHint("eclipselink.maintain-cache", "true");
        return query;
    }

    protected <R> R readThroughCache(String cacheKey, Supplier<R> dbQuery) {
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
            throw unavailableReadException("readThroughCache", ex);
        }
    }

    protected String filterKey(Filter<F> filter) {
        return DaoQueryCacheFallbackSupport.filterKey(filter);
    }

    private List<T> fallbackPageFromFindAllCache(Integer page, Integer pageSize, Filter<F> filter) {
        return DaoQueryCacheFallbackSupport.fallbackPageFromFindAllCache(entityClass, page, pageSize, filter);
    }

    private Long fallbackCountFromFindAllCache(Filter<F> filter) {
        return DaoQueryCacheFallbackSupport.fallbackCountFromFindAllCache(entityClass, filter);
    }

    private StorageUnavailableException unavailableReadException(String operation, Throwable cause) {
        return new StorageUnavailableException(
                "Primary database is temporarily unavailable and no cached data is available for " + operation + ".",
                cause
        );
    }
}
