package me.zinch.is.islab2.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.websocket.Session;
import me.zinch.is.islab2.models.dto.coordinates.CoordinatesDto;
import me.zinch.is.islab2.models.dto.imports.ImportConflictDto;
import me.zinch.is.islab2.models.dto.imports.ImportOperationDto;
import me.zinch.is.islab2.models.dto.user.UserDto;
import me.zinch.is.islab2.models.dto.user.UserShortDto;
import me.zinch.is.islab2.models.dto.vehicle.VehicleDto;
import me.zinch.is.islab2.models.entities.Coordinates;
import me.zinch.is.islab2.models.entities.ImportConflict;
import me.zinch.is.islab2.models.entities.ImportOperation;
import me.zinch.is.islab2.models.entities.User;
import me.zinch.is.islab2.models.entities.Vehicle;
import me.zinch.is.islab2.models.events.ws.WsEvent;
import me.zinch.is.islab2.models.ws.WebSocketResponse;
import me.zinch.is.islab2.models.ws.WsAction;
import me.zinch.is.islab2.models.ws.WsEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class WebSocketService {
    private final Map<Session, SessionInfo> sessions = new ConcurrentHashMap<>();

    public void add(Session session, User user) {
        if (user == null) {
            return;
        }
        sessions.put(session, new SessionInfo(user.getId(), user.getIsAdmin()));
    }

    public void remove(Session session) {
        sessions.remove(session);
    }

    public void sendEvent(WsEntity entity,
                          WsAction action,
                          Integer id,
                          Object payloadDto) {
        try (Jsonb builder = JsonbBuilder.create()) {
            WebSocketResponse<Object> event = new WebSocketResponse<>("event", entity.getValue(), action.getValue(), id, payloadDto);
            String json = builder.toJson(event);
            Integer ownerId = resolveOwnerId(entity, payloadDto);
            broadcast(json, ownerId);
        } catch (Exception ignored) { }
    }

    public void sendEvent(WsEvent event) {
        this.sendEvent(event.getEntity(), event.getAction(), event.getId(), event.getPayloadDto());
    }

    private void broadcast(String jsonMessage, Integer ownerId) {
        sessions.forEach((session, info) -> {
            if (!info.isAdmin && (ownerId == null || !ownerId.equals(info.userId))) {
                return;
            }
            try {
                session.getBasicRemote().sendText(jsonMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Integer resolveOwnerId(WsEntity entity, Object payloadDto) {
        if (payloadDto == null) {
            return null;
        }
        return switch (entity) {
            case COORDINATES -> resolveCoordinatesOwner(payloadDto);
            case VEHICLE -> resolveVehicleOwner(payloadDto);
            case IMPORT_OPERATION -> resolveImportOperationOwner(payloadDto);
            case IMPORT_CONFLICT -> resolveImportConflictOwner(payloadDto);
            case USER -> resolveUserOwner(payloadDto);
            default -> null;
        };
    }

    private Integer resolveOwnerId(UserShortDto owner) {
        return owner == null ? null : owner.getId();
    }

    private Integer resolveCoordinatesOwner(Object payloadDto) {
        if (payloadDto instanceof Coordinates coordinates) {
            return coordinates.getOwner().getId();
        }
        if (payloadDto instanceof CoordinatesDto coordinatesDto) {
            return resolveOwnerId(coordinatesDto.getOwner());
        }
        return null;
    }

    private Integer resolveVehicleOwner(Object payloadDto) {
        if (payloadDto instanceof Vehicle vehicle) {
            return vehicle.getOwner().getId();
        }
        if (payloadDto instanceof VehicleDto vehicleDto) {
            return resolveOwnerId(vehicleDto.getOwner());
        }
        return null;
    }

    private Integer resolveImportOperationOwner(Object payloadDto) {
        if (payloadDto instanceof ImportOperation operation) {
            return operation.getUser().getId();
        }
        if (payloadDto instanceof ImportOperationDto operationDto) {
            return resolveOwnerId(operationDto.getUser());
        }
        return null;
    }

    private Integer resolveImportConflictOwner(Object payloadDto) {
        if (payloadDto instanceof ImportConflict conflict) {
            return conflict.getOperation().getUser().getId();
        }
        if (payloadDto instanceof ImportConflictDto conflictDto) {
            return conflictDto.getUserId();
        }
        return null;
    }

    private Integer resolveUserOwner(Object payloadDto) {
        if (payloadDto instanceof User user) {
            return user.getId();
        }
        if (payloadDto instanceof UserDto userDto) {
            return userDto.getId();
        }
        if (payloadDto instanceof UserShortDto userShortDto) {
            return userShortDto.getId();
        }
        return null;
    }

    private record SessionInfo(Integer userId, boolean isAdmin) {
    }
}
