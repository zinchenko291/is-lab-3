package me.zinch.is.islab2.services;

import jakarta.enterprise.event.Event;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import me.zinch.is.islab2.Config;
import me.zinch.is.islab2.exceptions.ResourceNotFoundException;
import me.zinch.is.islab2.models.dao.interfaces.IDao;
import me.zinch.is.islab2.models.dto.IMapper;
import me.zinch.is.islab2.models.fields.EntityField;
import me.zinch.is.islab2.models.fields.Filter;
import me.zinch.is.islab2.models.fields.Page;
import me.zinch.is.islab2.models.fields.SortDirection;
import me.zinch.is.islab2.models.ws.WsAction;
import me.zinch.is.islab2.models.ws.WsEntity;
import me.zinch.is.islab2.models.events.ws.WsEvent;

import java.util.List;

/**
 * Abstract CRUD Service
 * @param <E> Entity
 * @param <F> EntityFields
 * @param <D> DTO
 * @param <I> DTO without ID field
 */
public abstract class AbstractService<E, F extends EntityField, D, I> {
    protected IDao<E, F> dao;
    protected IMapper<E, D, I> mapper;
    protected Event<WsEvent> wsEvent;

    @PersistenceContext(unitName = Config.UNIT_NAME)
    protected EntityManager em;

    protected AbstractService() {}

    protected AbstractService(IDao<E, F> dao,
                              IMapper<E, D, I> mapper,
                              Event<WsEvent> wsEvent) {
        this.dao = dao;
        this.mapper = mapper;
        this.wsEvent = wsEvent;
    }

    public Page<D> findAll(F field, String value, SortDirection orderBy, Integer pageSize, Integer page) {
        Filter<F> filter = new Filter<>(field, value, orderBy);
        Long counter = dao.countPaged(filter);
        List<E> entity = dao.findAllPaged(page, pageSize, filter);
        return new Page<>(
                counter,
                entity.stream().map(this.mapper::entityToDto).toList()
        );
    }

    public D findById(Integer id) {
        return dao.findById(id)
                .map(this.mapper::entityToDto)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceExceptionMessage(id)));
    }

    @Transactional()
    public D create(I dto) {
        E entity = dao.create(mapper.idLessDtoToEntity(dto));
        wsEvent.fire(new WsEvent(getWsEntity(), WsAction.CREATE, getEntityId(entity), entity));
        return mapper.entityToDto(entity);
    }

    public abstract D updateById(Integer id, D dto);

    @Transactional
    public D deleteById(Integer id) {
        setRepeatableReadIsolation();
        E entity = dao.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceExceptionMessage(id)));
        entity = dao.delete(entity);
        wsEvent.fire(new WsEvent(
                getWsEntity(), WsAction.DELETE, getEntityId(entity), entity
        ));
        return mapper.entityToDto(entity);
    }

    protected abstract String getResourceExceptionMessage(Integer id);

    protected abstract WsEntity getWsEntity();
    protected abstract Integer getEntityId(E entity);

    protected void setRepeatableReadIsolation() {
        if (em == null) {
            return;
        }
        em.createNativeQuery("SET TRANSACTION ISOLATION LEVEL REPEATABLE READ").executeUpdate();
    }
}
