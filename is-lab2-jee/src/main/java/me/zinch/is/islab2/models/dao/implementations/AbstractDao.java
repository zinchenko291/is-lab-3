package me.zinch.is.islab2.models.dao.implementations;

import me.zinch.is.islab2.Config;
import me.zinch.is.islab2.exceptions.ConstraintException;
import me.zinch.is.islab2.exceptions.FieldValueConvertException;
import me.zinch.is.islab2.exceptions.ConflictException;
import me.zinch.is.islab2.models.dao.interfaces.IConverter;
import me.zinch.is.islab2.models.dao.interfaces.IDao;
import me.zinch.is.islab2.models.fields.EntityField;
import me.zinch.is.islab2.models.fields.Filter;
import org.eclipse.persistence.exceptions.ConversionException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        return Optional.ofNullable(em.find(entityClass, id));
    }

    @Override
    public Optional<T> findByIdForUpdate(Integer id) {
        return Optional.ofNullable(em.find(entityClass, id, LockModeType.PESSIMISTIC_WRITE));
    }

    @Override
    public List<T> findAll() {
        return em.createQuery(String.format("SELECT c FROM %s c", entityClass.getSimpleName()), entityClass)
                .getResultList();
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

        TypedQuery<T> q = em.createQuery(cq);
        q.setMaxResults(pageSize);
        q.setFirstResult(page * pageSize);
        try {
            return q.getResultList();
        } catch (PersistenceException e) {
            if (e.getCause() instanceof ConversionException && filter.getValue() != null && !filter.getValue().isEmpty()) {
                throw new FieldValueConvertException(
                    String.format("Не удалось сконвертировать значение %s для поля %s", filter.getValue(), filter.getField().getValue())
                );
            }
            throw e;
        }
    }

    @Override
    public Long count() {
        return em.createQuery(String.format("SELECT COUNT(c) FROM %s c", entityClass.getSimpleName()), Long.class)
                .getSingleResult();
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

        try {
            return em.createQuery(cq).getSingleResult();
        } catch (PersistenceException e) {
            if (e.getCause() instanceof ConversionException && filter.getValue() != null && !filter.getValue().isEmpty()) {
                throw new FieldValueConvertException(
                        String.format("Не удалось сконвертировать значение %s для поля %s", filter.getValue(), filter.getField().getValue())
                );
            }
            throw e;
        }
    }

    @Override
    public T create(T entity) {
        try {
            em.persist(entity);
            em.flush();
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
}
