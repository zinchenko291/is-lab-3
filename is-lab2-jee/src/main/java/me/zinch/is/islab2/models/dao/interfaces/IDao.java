package me.zinch.is.islab2.models.dao.interfaces;

import me.zinch.is.islab2.models.fields.EntityField;
import me.zinch.is.islab2.models.fields.Filter;

import java.util.List;
import java.util.Optional;

public interface IDao<T, F extends EntityField> {
    public Optional<T> findById(Integer id);
    public Optional<T> findByIdForUpdate(Integer id);
    public List<T> findAll();
    public List<T> findAllPaged(Integer page, Integer pageSize, Filter<F> filter);
    public Long count();
    public Long countPaged(Filter<F> filter);
    public T create(T coordinates);
    public T update(T coordinates);
    public T delete(T coordinates);
}
