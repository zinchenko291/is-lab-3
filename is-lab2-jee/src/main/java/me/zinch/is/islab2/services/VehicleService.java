package me.zinch.is.islab2.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import me.zinch.is.islab2.exceptions.ForbiddenException;
import me.zinch.is.islab2.exceptions.ResourceNotFoundException;
import me.zinch.is.islab2.models.dao.implementations.CoordinatesDao;
import me.zinch.is.islab2.models.dao.implementations.VehicleDao;
import me.zinch.is.islab2.models.dto.vehicle.VehicleDto;
import me.zinch.is.islab2.models.dto.vehicle.VehicleMapper;
import me.zinch.is.islab2.models.dto.vehicle.VehicleWithoutIdDto;
import me.zinch.is.islab2.models.entities.Coordinates;
import me.zinch.is.islab2.models.entities.FuelType;
import me.zinch.is.islab2.models.entities.User;
import me.zinch.is.islab2.models.entities.Vehicle;
import me.zinch.is.islab2.models.fields.Filter;
import me.zinch.is.islab2.models.fields.Page;
import me.zinch.is.islab2.models.fields.Range;
import me.zinch.is.islab2.models.fields.SortDirection;
import me.zinch.is.islab2.models.fields.VehicleField;
import me.zinch.is.islab2.models.ws.WsAction;
import me.zinch.is.islab2.models.ws.WsEntity;
import me.zinch.is.islab2.models.events.ws.WsEvent;

import java.util.List;

@ApplicationScoped
public class VehicleService extends AbstractService<Vehicle, VehicleField, VehicleDto, VehicleWithoutIdDto> {
    private static final long EDIT_WINDOW_MS = 24L * 60L * 60L * 1000L;
    private VehicleDao vehicleDao;
    private CoordinatesDao coordinatesDao;

    public VehicleService() {
        super();
    }

    @Inject
    public VehicleService(VehicleDao vehicleDao, CoordinatesDao coordinatesDao, VehicleMapper vehicleMapper, Event<WsEvent> wsEvent) {
        super(vehicleDao, vehicleMapper, wsEvent);
        this.vehicleDao = vehicleDao;
        this.coordinatesDao = coordinatesDao;
    }

    @Override
    @Transactional
    public VehicleDto updateById(Integer id, VehicleDto dto) {
        setRepeatableReadIsolation();
        return vehicleDao.findByIdForUpdate(id)
                .map(v -> {
                    ensureEditable(v);
                    Vehicle vehicle = mapper.dtoToEntity(dto);
                    vehicle.setId(v.getId());
                    return vehicle;
                })
                .map(vehicleDao::update)
                .map(mapper::entityToDto)
                .map(this::sendUpdateEvent)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceExceptionMessage(id)));
    }

    @Override
    protected String getResourceExceptionMessage(Integer id) {
        return String.format("Не существует транспортного средства по id = %s", id);
    }

    public VehicleDto findMinEnginePower() {
        return vehicleDao.findMinEnginePower()
                .map(mapper::entityToDto)
                .orElseThrow(() -> new ResourceNotFoundException("ТС с минимальной мощностью не найдено"));
    }

    public Long countGtFuelType(FuelType fuelType) {
        return vehicleDao.countGtFuelType(fuelType);
    }
    public Page<VehicleDto> findByNameSubstring(Integer page, Integer pageSize, String name) {
        List<Vehicle> vehicles = vehicleDao.findByNameSubstring(page, pageSize, name);
        Long total = vehicleDao.countByNameSubstring(name);

        List<VehicleDto> dtos = vehicles.stream()
                .map(mapper::entityToDto)
                .toList();

        return new Page<>(total, dtos);
    }

    public Page<VehicleDto> findByEnginePowerRange(Integer page, Integer pageSize, Range<Integer> range) {
        List<Vehicle> vehicles = vehicleDao.findByEnginePowerRange(page, pageSize, range);
        Long total = vehicleDao.countByEnginePowerRange(range);

        List<VehicleDto> dtos = vehicles.stream()
                .map(mapper::entityToDto)
                .toList();

        return new Page<>(total, dtos);
    }

    @Transactional
    public VehicleDto resetDistanceTravelledById(Integer id) {
        setRepeatableReadIsolation();
        return vehicleDao.resetDistanceTravelledById(id)
            .map(mapper::entityToDto)
            .map(this::sendUpdateEvent)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            String.format("Не существует транспортного средства по id = %s", id)
                    )
            );
    }

    public Page<VehicleDto> findAllForUser(User user, VehicleField field, String value, SortDirection orderBy, Integer pageSize, Integer page) {
        if (user.getIsAdmin()) {
            return super.findAll(field, value, orderBy, pageSize, page);
        }
        Filter<VehicleField> filter = new Filter<>(field, value, orderBy);
        Long counter = vehicleDao.countPagedByOwner(filter, user.getId());
        List<Vehicle> vehicles = vehicleDao.findAllPagedByOwner(page, pageSize, filter, user.getId());

        List<VehicleDto> dtos = vehicles.stream()
                .map(mapper::entityToDto)
                .toList();

        return new Page<>(counter, dtos);
    }

    public VehicleDto findByIdForUser(User user, Integer id) {
        Vehicle vehicle = vehicleDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceExceptionMessage(id)));
        enforceOwner(user, vehicle);
        return mapper.entityToDto(vehicle);
    }

    @Transactional
    public VehicleDto createForUser(User user, VehicleWithoutIdDto dto) {
        Vehicle vehicle = mapper.idLessDtoToEntity(dto);
        Coordinates coordinates = coordinatesDao.findById(vehicle.getCoordinates().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Coordinates not found"));
        enforceOwner(user, coordinates);
        vehicle.setCoordinates(coordinates);
        vehicle.setOwner(user);
        vehicle = vehicleDao.create(vehicle);
        wsEvent.fire(new WsEvent(getWsEntity(), WsAction.CREATE, getEntityId(vehicle), vehicle));
        return mapper.entityToDto(vehicle);
    }

    @Transactional
    public VehicleDto updateByIdForUser(User user, Integer id, VehicleDto dto) {
        setRepeatableReadIsolation();
        return vehicleDao.findByIdForUpdate(id)
                .map(existing -> {
                    enforceOwner(user, existing);
                    ensureEditable(existing);
                    Vehicle vehicle = mapper.dtoToEntity(dto);
                    Coordinates coordinates = coordinatesDao.findById(vehicle.getCoordinates().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Coordinates not found"));
                    enforceOwner(user, coordinates);
                    vehicle.setCoordinates(coordinates);
                    vehicle.setId(existing.getId());
                    vehicle.setOwner(existing.getOwner());
                    return vehicle;
                })
                .map(vehicleDao::update)
                .map(mapper::entityToDto)
                .map(this::sendUpdateEvent)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceExceptionMessage(id)));
    }

    @Transactional
    public VehicleDto deleteByIdForUser(User user, Integer id) {
        setRepeatableReadIsolation();
        Vehicle vehicle = vehicleDao.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceExceptionMessage(id)));
        enforceOwner(user, vehicle);
        vehicle = vehicleDao.delete(vehicle);
        wsEvent.fire(new WsEvent(getWsEntity(), WsAction.DELETE, getEntityId(vehicle), vehicle));
        return mapper.entityToDto(vehicle);
    }

    public VehicleDto findMinEnginePowerForUser(User user) {
        if (user.getIsAdmin()) {
            return findMinEnginePower();
        }
        return vehicleDao.findMinEnginePowerByOwner(user.getId())
                .map(mapper::entityToDto)
                .orElseThrow(() -> new ResourceNotFoundException("ТС с минимальной мощностью не найдено"));
    }

    public Long countGtFuelTypeForUser(User user, FuelType fuelType) {
        if (user.getIsAdmin()) {
            return vehicleDao.countGtFuelType(fuelType);
        }
        return vehicleDao.countGtFuelTypeByOwner(user.getId(), fuelType);
    }

    public Page<VehicleDto> findByNameSubstringForUser(User user, Integer page, Integer pageSize, String name) {
        if (user.getIsAdmin()) {
            return findByNameSubstring(page, pageSize, name);
        }
        List<Vehicle> vehicles = vehicleDao.findByNameSubstringByOwner(page, pageSize, name, user.getId());
        Long total = vehicleDao.countByNameSubstringByOwner(name, user.getId());

        List<VehicleDto> dtos = vehicles.stream()
                .map(mapper::entityToDto)
                .toList();
        return new Page<>(total, dtos);
    }

    public Page<VehicleDto> findByEnginePowerRangeForUser(User user, Integer page, Integer pageSize, Range<Integer> range) {
        if (user.getIsAdmin()) {
            return findByEnginePowerRange(page, pageSize, range);
        }
        List<Vehicle> vehicles = vehicleDao.findByEnginePowerRangeByOwner(page, pageSize, range, user.getId());
        Long total = vehicleDao.countByEnginePowerRangeByOwner(range, user.getId());

        List<VehicleDto> dtos = vehicles.stream()
                .map(mapper::entityToDto)
                .toList();
        return new Page<>(total, dtos);
    }

    @Transactional
    public VehicleDto resetDistanceTravelledByIdForUser(User user, Integer id) {
        setRepeatableReadIsolation();
        Vehicle vehicle = vehicleDao.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceExceptionMessage(id)));
        enforceOwner(user, vehicle);
        return vehicleDao.resetDistanceTravelledById(id)
                .map(mapper::entityToDto)
                .map(this::sendUpdateEvent)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                String.format("Не существует транспортного средства по id = %s", id)
                        )
                );
    }

    private void enforceOwner(User user, Vehicle vehicle) {
        if (user.getIsAdmin()) {
            return;
        }
        if (!isOwner(user, vehicle)) {
            throw new ForbiddenException("Forbidden");
        }
    }

    private void enforceOwner(User user, Coordinates coordinates) {
        if (user.getIsAdmin()) {
            return;
        }
        if (coordinates.getOwner().getId() != user.getId()) {
            throw new ForbiddenException("Forbidden");
        }
    }

    private boolean isOwner(User user, Vehicle vehicle) {
        return vehicle.getOwner().getId() == user.getId();
    }

    private VehicleDto sendUpdateEvent(VehicleDto vehicleDto) {
        wsEvent.fire(new WsEvent(WsEntity.VEHICLE, WsAction.UPDATE, vehicleDto.getId(), vehicleDto));
        return vehicleDto;
    }

    private void ensureEditable(Vehicle vehicle) {
        if (vehicle.getCreationDate() == null) {
            return;
        }
        long ageMs = System.currentTimeMillis() - vehicle.getCreationDate().getTime();
        if (ageMs > EDIT_WINDOW_MS) {
            throw new ForbiddenException("Транспорт можно редактировать только в течение 24 часов после создания");
        }
    }

    @Override
    protected WsEntity getWsEntity() {
        return WsEntity.VEHICLE;
    }

    @Override
    protected Integer getEntityId(Vehicle entity) {
        return entity.getId();
    }
}
