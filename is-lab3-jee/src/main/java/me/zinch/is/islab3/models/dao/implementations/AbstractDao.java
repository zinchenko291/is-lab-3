package me.zinch.is.islab3.models.dao.implementations;

import me.zinch.is.islab3.Config;
import me.zinch.is.islab3.exceptions.ConstraintException;
import me.zinch.is.islab3.exceptions.FieldValueConvertException;
import me.zinch.is.islab3.exceptions.ConflictException;
import me.zinch.is.islab3.models.dao.interfaces.IConverter;
import me.zinch.is.islab3.models.dao.interfaces.IDao;
import me.zinch.is.islab3.models.fields.EntityField;
import me.zinch.is.islab3.models.fields.Filter;
import me.zinch.is.islab3.cache.DatabaseFailureDetector;
import me.zinch.is.islab3.cache.InfinispanL2CacheBridge;
import me.zinch.is.islab3.cache.LogL2CacheStats;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@LogL2CacheStats
public abstract class AbstractDao<T, F extends EntityField> implements IDao<T, F> {
    @PersistenceContext(unitName = Config.UNIT_NAME)
    protected EntityManager em;

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
        if (DatabaseFailureDetector.isDatabaseLikelyDown()) {
            Object cached = InfinispanL2CacheBridge.get(entityClass.getName(), id);
            if (entityClass.isInstance(cached)) {
                return Optional.of(entityClass.cast(cached));
            }
        }
        try {
            Optional<T> result = Optional.ofNullable(em.find(entityClass, id));
            DatabaseFailureDetector.markSuccess();
            return result;
        } catch (PersistenceException e) {
            if (!DatabaseFailureDetector.isDbCommunicationFailure(e)) {
                throw e;
            }
            DatabaseFailureDetector.markFailure(e);
            Object cached = InfinispanL2CacheBridge.get(entityClass.getName(), id);
            if (entityClass.isInstance(cached)) {
                return Optional.of(entityClass.cast(cached));
            }
            throw e;
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
        if (DatabaseFailureDetector.isDatabaseLikelyDown()) {
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
            DatabaseFailureDetector.markSuccess();
            InfinispanL2CacheBridge.putQueryResult(cacheKey, result);
            return result;
        } catch (PersistenceException e) {
            if (isFieldConversionError(e, filter)) {
                throw new FieldValueConvertException(
                    String.format("Не удалось сконвертировать значение %s для поля %s", filter.getValue(), filter.getField().getValue())
                );
            }
            if (DatabaseFailureDetector.isDbCommunicationFailure(e)) {
                DatabaseFailureDetector.markFailure(e);
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
        if (DatabaseFailureDetector.isDatabaseLikelyDown()) {
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
            DatabaseFailureDetector.markSuccess();
            InfinispanL2CacheBridge.putQueryResult(cacheKey, result);
            return result;
        } catch (PersistenceException e) {
            if (isFieldConversionError(e, filter)) {
                throw new FieldValueConvertException(
                        String.format("Не удалось сконвертировать значение %s для поля %s", filter.getValue(), filter.getField().getValue())
                );
            }
            if (DatabaseFailureDetector.isDbCommunicationFailure(e)) {
                DatabaseFailureDetector.markFailure(e);
                Object cached = InfinispanL2CacheBridge.getQueryResult(cacheKey);
                if (cached instanceof Long restored) {
                    return restored;
                }
                Long fromFindAll = fallbackCountFromFindAllCache(filter);
                if (fromFindAll != null) {
                    return fromFindAll;
                }
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
            throw mapPersistenceException(e);
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
            throw mapPersistenceException(e);
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
            throw mapPersistenceException(e);
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

    private RuntimeException mapPersistenceException(PersistenceException e) {
        if (hasSqlState(e, "23505")) {
            return new ConflictException("Нарушено ограничение уникальности.");
        }
        if (hasSqlState(e, "23503")) {
            return new ConstraintException("Не удалось удалить сущность из-за связи.");
        }
        if (hasSqlState(e, "40001")) {
            return new ConflictException("Конфликт транзакции. Повторите запрос.");
        }
        return e;
    }

    private boolean hasSqlState(Throwable ex, String sqlState) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof SQLException && sqlState.equals(((SQLException) current).getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isFieldConversionError(PersistenceException e, Filter<F> filter) {
        if (filter == null || filter.getValue() == null || filter.getValue().isEmpty()) {
            return false;
        }
        Throwable current = e;
        while (current != null) {
            if (current instanceof NumberFormatException || current instanceof IllegalArgumentException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    protected <Q extends Query> Q cacheQuery(Q query) {
        query.setHint("eclipselink.query-results-cache", "true");
        query.setHint("eclipselink.maintain-cache", "true");
        return query;
    }

    protected <R> R readThroughCache(String cacheKey, Supplier<R> dbQuery) {
        if (DatabaseFailureDetector.isDatabaseLikelyDown()) {
            Object cached = InfinispanL2CacheBridge.getQueryResult(cacheKey);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                R restored = (R) cached;
                return restored;
            }
        }
        try {
            R result = dbQuery.get();
            DatabaseFailureDetector.markSuccess();
            InfinispanL2CacheBridge.putQueryResult(cacheKey, result);
            return result;
        } catch (RuntimeException ex) {
            if (!DatabaseFailureDetector.isDbCommunicationFailure(ex)) {
                throw ex;
            }
            DatabaseFailureDetector.markFailure(ex);
            Object cached = InfinispanL2CacheBridge.getQueryResult(cacheKey);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                R restored = (R) cached;
                return restored;
            }
            throw ex;
        }
    }

    protected String filterKey(Filter<F> filter) {
        if (filter == null) {
            return "null";
        }
        String field = filter.getField() == null ? "null" : filter.getField().getValue();
        String value = filter.getValue() == null ? "null" : filter.getValue();
        String direction = filter.getSortDirection() == null ? "null" : filter.getSortDirection().getValue();
        return field + "|" + value + "|" + direction;
    }

    private List<T> fallbackPageFromFindAllCache(Integer page, Integer pageSize, Filter<F> filter) {
        if (!isUnfiltered(filter)) {
            return null;
        }
        Object allCached = InfinispanL2CacheBridge.getQueryResult("findAll:" + entityClass.getName());
        if (!(allCached instanceof List<?> all)) {
            return null;
        }
        int from = Math.max(0, page * pageSize);
        if (from >= all.size()) {
            return List.of();
        }
        int to = Math.min(all.size(), from + pageSize);
        @SuppressWarnings("unchecked")
        List<T> pageSlice = (List<T>) all.subList(from, to);
        return pageSlice;
    }

    private Long fallbackCountFromFindAllCache(Filter<F> filter) {
        if (!isUnfiltered(filter)) {
            return null;
        }
        Object countCached = InfinispanL2CacheBridge.getQueryResult("count:" + entityClass.getName());
        if (countCached instanceof Long count) {
            return count;
        }
        Object allCached = InfinispanL2CacheBridge.getQueryResult("findAll:" + entityClass.getName());
        if (allCached instanceof List<?> all) {
            return (long) all.size();
        }
        return null;
    }

    private boolean isUnfiltered(Filter<F> filter) {
        if (filter == null) {
            return true;
        }
        if (filter.getField() != null) {
            return false;
        }
        String value = filter.getValue();
        return value == null || value.isBlank();
    }
}
