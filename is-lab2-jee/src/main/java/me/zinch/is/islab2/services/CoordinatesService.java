package me.zinch.is.islab2.services;

import jakarta.enterprise.event.Event;
import me.zinch.is.islab2.exceptions.ForbiddenException;
import me.zinch.is.islab2.exceptions.ResourceNotFoundException;
import me.zinch.is.islab2.models.dao.implementations.CoordinatesDao;
import me.zinch.is.islab2.models.dto.coordinates.CoordinatesDto;
import me.zinch.is.islab2.models.dto.coordinates.CoordinatesMapper;
import me.zinch.is.islab2.models.dto.coordinates.CoordinatesWithoutIdDto;
import me.zinch.is.islab2.models.entities.Coordinates;
import me.zinch.is.islab2.models.entities.User;
import me.zinch.is.islab2.models.fields.CoordinatesField;
import me.zinch.is.islab2.models.fields.Filter;
import me.zinch.is.islab2.models.fields.Page;
import me.zinch.is.islab2.models.fields.SortDirection;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import me.zinch.is.islab2.models.ws.WsAction;
import me.zinch.is.islab2.models.ws.WsEntity;
import me.zinch.is.islab2.models.events.ws.WsEvent;


@ApplicationScoped
public class CoordinatesService extends AbstractService<Coordinates, CoordinatesField, CoordinatesDto, CoordinatesWithoutIdDto> {
    private CoordinatesDao coordinatesDao;

    public CoordinatesService() {}

    @Inject
    public CoordinatesService(
            CoordinatesDao dao,
            CoordinatesMapper mapper,
            Event<WsEvent> wsEvent
    ) {
        super(dao, mapper, wsEvent);
        this.coordinatesDao = dao;
    }

    @Override
    @Transactional
    public CoordinatesDto updateById(Integer id, CoordinatesDto dto) {
        setRepeatableReadIsolation();
        return dao.findByIdForUpdate(id)
                .map(obj -> {
                    Coordinates coordinates = mapper.dtoToEntity(dto);
                    coordinates.setId(obj.getId());
                    return coordinates;
                })
                .map(dao::update)
                .map(mapper::entityToDto)
                .map(this::sendUpdateEvent)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceExceptionMessage(id)));
    }

    @Override
    protected String getResourceExceptionMessage(Integer id) {
        return String.format("Не существует координаты по id = %s", id);
    }

    @Override
    protected WsEntity getWsEntity() {
        return WsEntity.COORDINATES;
    }

    @Override
    protected Integer getEntityId(Coordinates entity) {
        return entity.getId();
    }

    private CoordinatesDto sendUpdateEvent(CoordinatesDto dto) {
        wsEvent.fire(new WsEvent(WsEntity.COORDINATES, WsAction.UPDATE, dto.getId(), dto));
        return dto;
    }

    public Page<CoordinatesDto> findAllForUser(User user, CoordinatesField field, String value, SortDirection orderBy, Integer pageSize, Integer page) {
        if (user.getIsAdmin()) {
            return super.findAll(field, value, orderBy, pageSize, page);
        }
        Filter<CoordinatesField> filter = new Filter<>(field, value, orderBy);
        Long counter = coordinatesDao.countPagedByOwner(filter, user.getId());
        return new Page<>(
                counter,
                coordinatesDao.findAllPagedByOwner(page, pageSize, filter, user.getId())
                        .stream()
                        .map(mapper::entityToDto)
                        .toList()
        );
    }

    public CoordinatesDto findByIdForUser(User user, Integer id) {
        Coordinates coordinates = coordinatesDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceExceptionMessage(id)));
        enforceOwner(user, coordinates);
        return mapper.entityToDto(coordinates);
    }

    @Transactional
    public CoordinatesDto createForUser(User user, CoordinatesWithoutIdDto dto) {
        Coordinates coordinates = mapper.idLessDtoToEntity(dto);
        coordinates.setOwner(user);
        coordinates = coordinatesDao.create(coordinates);
        wsEvent.fire(new WsEvent(getWsEntity(), WsAction.CREATE, coordinates.getId(), coordinates));
        return mapper.entityToDto(coordinates);
    }

    @Transactional
    public CoordinatesDto updateByIdForUser(User user, Integer id, CoordinatesDto dto) {
        setRepeatableReadIsolation();
        return coordinatesDao.findByIdForUpdate(id)
                .map(existing -> {
                    enforceOwner(user, existing);
                    Coordinates coordinates = mapper.dtoToEntity(dto);
                    coordinates.setId(existing.getId());
                    coordinates.setOwner(existing.getOwner());
                    return coordinates;
                })
                .map(coordinatesDao::update)
                .map(mapper::entityToDto)
                .map(this::sendUpdateEvent)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceExceptionMessage(id)));
    }

    @Transactional
    public CoordinatesDto deleteByIdForUser(User user, Integer id) {
        setRepeatableReadIsolation();
        Coordinates coordinates = coordinatesDao.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceExceptionMessage(id)));
        enforceOwner(user, coordinates);
        coordinates = coordinatesDao.delete(coordinates);
        wsEvent.fire(new WsEvent(getWsEntity(), WsAction.DELETE, coordinates.getId(), coordinates));
        return mapper.entityToDto(coordinates);
    }

    private void enforceOwner(User user, Coordinates coordinates) {
        if (user.getIsAdmin()) {
            return;
        }
        if (coordinates.getOwner().getId() != user.getId()) {
            throw new ForbiddenException("Forbidden");
        }
    }
}
