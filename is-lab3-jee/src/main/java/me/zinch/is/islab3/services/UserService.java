package me.zinch.is.islab3.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import me.zinch.is.islab3.exceptions.ResourceNotFoundException;
import me.zinch.is.islab3.models.dao.implementations.UserDao;
import me.zinch.is.islab3.models.dto.user.UserDto;
import me.zinch.is.islab3.models.dto.user.UserMapper;
import me.zinch.is.islab3.models.dto.user.UserWithoutIdDto;
import me.zinch.is.islab3.models.entities.User;
import me.zinch.is.islab3.models.events.ws.WsEvent;
import me.zinch.is.islab3.models.fields.UserField;
import me.zinch.is.islab3.models.ws.WsAction;
import me.zinch.is.islab3.models.ws.WsEntity;

@ApplicationScoped
public class UserService extends AbstractService<User, UserField, UserDto, UserWithoutIdDto> {
    public UserService() {
    }

    @Inject
    public UserService(UserDao dao, UserMapper mapper, Event<WsEvent> wsEvent) {
        super(dao, mapper, wsEvent);
    }

    @Override
    @Transactional
    public UserDto updateById(Integer id, UserDto dto) {
        return dao.findById(id)
                .map(obj -> {
                    User user = mapper.dtoToEntity(dto);
                    user.setId(obj.getId());
                    return user;
                })
                .map(dao::update)
                .map(mapper::entityToDto)
                .map(this::sendUpdateEvent)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceExceptionMessage(id)));
    }

    @Override
    protected String getResourceExceptionMessage(Integer id) {
        return String.format("User with id = %s not found", id);
    }

    @Override
    protected WsEntity getWsEntity() {
        return WsEntity.USER;
    }

    @Override
    protected Integer getEntityId(User entity) {
        return entity.getId();
    }

    private UserDto sendUpdateEvent(UserDto dto) {
        wsEvent.fire(new WsEvent(WsEntity.USER, WsAction.UPDATE, dto.getId(), dto));
        return dto;
    }
}
