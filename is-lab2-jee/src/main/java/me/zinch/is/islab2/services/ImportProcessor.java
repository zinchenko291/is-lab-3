package me.zinch.is.islab2.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import me.zinch.is.islab2.Config;
import me.zinch.is.islab2.exceptions.ConstraintException;
import me.zinch.is.islab2.exceptions.DeserializingException;
import me.zinch.is.islab2.exceptions.ForbiddenException;
import me.zinch.is.islab2.exceptions.ResourceNotFoundException;
import me.zinch.is.islab2.models.dao.implementations.CoordinatesDao;
import me.zinch.is.islab2.models.dao.implementations.VehicleDao;
import me.zinch.is.islab2.models.dto.imports.ImportVehicleDto;
import me.zinch.is.islab2.models.dto.imports.ImportVehiclesDto;
import me.zinch.is.islab2.models.entities.Coordinates;
import me.zinch.is.islab2.models.entities.ImportConflictResolution;
import me.zinch.is.islab2.models.entities.ImportFormat;
import me.zinch.is.islab2.models.entities.ImportOperation;
import me.zinch.is.islab2.models.entities.User;
import me.zinch.is.islab2.models.entities.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class ImportProcessor {
    private static final long EDIT_WINDOW_MS = 24L * 60L * 60L * 1000L;

    private final VehicleDao vehicleDao;
    private final CoordinatesDao coordinatesDao;
    private final Validator validator;

    @PersistenceContext(unitName = Config.UNIT_NAME)
    private EntityManager em;

    public ImportProcessor() {
        this.vehicleDao = null;
        this.coordinatesDao = null;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Inject
    public ImportProcessor(VehicleDao vehicleDao, CoordinatesDao coordinatesDao) {
        this.vehicleDao = vehicleDao;
        this.coordinatesDao = coordinatesDao;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Transactional(TxType.REQUIRES_NEW)
    public ImportResult processImport(ImportOperation operation, String payload, Map<Integer, ImportConflictResolution> resolutions) {
        setRepeatableReadIsolation();
        List<ImportVehicleDto> vehicles = parseVehicles(operation.getFormat(), payload);
        validateVehicles(vehicles);

        List<ImportConflictInfo> conflicts = detectConflicts(operation.getUser(), vehicles);
        if (resolutions == null) {
            if (!conflicts.isEmpty()) {
                return new ImportResult(0, conflicts);
            }
        } else {
            List<ImportConflictInfo> unresolved = conflicts.stream()
                    .filter(conflict -> {
                        ImportConflictResolution resolution = resolutions.get(conflict.vehicleIndex());
                        return resolution == null || resolution == ImportConflictResolution.UNRESOLVED;
                    })
                    .toList();
            if (!unresolved.isEmpty()) {
                return new ImportResult(0, unresolved);
            }
        }

        int added = 0;
        for (int i = 0; i < vehicles.size(); i++) {
            ImportVehicleDto vehicleDto = vehicles.get(i);
            ImportConflictResolution resolution = resolutions == null ? null : resolutions.get(i);
            if (resolution == ImportConflictResolution.SKIP) {
                continue;
            }

            Optional<Coordinates> existingCoordinates = coordinatesDao.findByXY(
                    vehicleDto.getCoordinates().getX(),
                    vehicleDto.getCoordinates().getY()
            );

            Integer targetId = vehicleDto.getId();
            if (targetId == null && resolution == ImportConflictResolution.OVERWRITE) {
                int finalI = i;
                targetId = conflicts.stream()
                        .filter(c -> c.vehicleIndex() == finalI)
                        .map(ImportConflictInfo::existingVehicleId)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
            }

            if (targetId != null) {
                Vehicle existing = vehicleDao.findById(targetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Транспорт не найден"));
                enforceOwner(operation.getUser(), existing);
                ensureEditable(existing);
                Coordinates coordinates = resolveCoordinatesForUpdate(operation.getUser(), vehicleDto, existingCoordinates);
                applyVehicleFields(existing, vehicleDto, coordinates);
                vehicleDao.update(existing);
                added++;
                continue;
            }

            Coordinates coordinates = existingCoordinates.orElseGet(() -> createCoordinates(operation.getUser(), vehicleDto));
            Vehicle created = new Vehicle();
            created.setOwner(operation.getUser());
            applyVehicleFields(created, vehicleDto, coordinates);
            vehicleDao.create(created);
            added++;
        }

        return new ImportResult(added, List.of());
    }

    private Coordinates resolveCoordinatesForUpdate(User user,
                                                    ImportVehicleDto vehicleDto,
                                                    Optional<Coordinates> existingCoordinates) {
        return existingCoordinates.orElseGet(() -> createCoordinates(user, vehicleDto));
    }

    private Coordinates createCoordinates(User user, ImportVehicleDto vehicleDto) {
        Coordinates coordinates = new Coordinates();
        coordinates.setX(vehicleDto.getCoordinates().getX());
        coordinates.setY(vehicleDto.getCoordinates().getY());
        coordinates.setOwner(user);
        return coordinatesDao.create(coordinates);
    }

    private void applyVehicleFields(Vehicle vehicle, ImportVehicleDto vehicleDto, Coordinates coordinates) {
        vehicle.setName(vehicleDto.getName());
        vehicle.setCoordinates(coordinates);
        vehicle.setType(vehicleDto.getType());
        vehicle.setEnginePower(vehicleDto.getEnginePower());
        vehicle.setNumberOfWheels(vehicleDto.getNumberOfWheels());
        vehicle.setCapacity(vehicleDto.getCapacity());
        vehicle.setDistanceTravelled(vehicleDto.getDistanceTravelled());
        vehicle.setFuelConsumption(vehicleDto.getFuelConsumption());
        vehicle.setFuelType(vehicleDto.getFuelType());
    }

    private List<ImportVehicleDto> parseVehicles(ImportFormat format, String payload) {
        if (payload == null) {
            throw new DeserializingException("Файл импорта недоступен");
        }
        ObjectMapper mapper = format == ImportFormat.XML
                ? new XmlMapper()
                : new ObjectMapper(new YAMLFactory());
        try {
            ImportVehiclesDto wrapper = mapper.readValue(payload, ImportVehiclesDto.class);
            if (wrapper != null && wrapper.getVehicles() != null && !wrapper.getVehicles().isEmpty()) {
                return wrapper.getVehicles();
            }
        } catch (Exception ignored) {
        }
        try {
            return mapper.readValue(payload, new TypeReference<List<ImportVehicleDto>>() {});
        } catch (Exception e) {
            throw new DeserializingException("Некорректный файл импорта");
        }
    }

    private void validateVehicles(List<ImportVehicleDto> vehicles) {
        List<String> violations = new ArrayList<>();
        for (int i = 0; i < vehicles.size(); i++) {
            ImportVehicleDto vehicle = vehicles.get(i);
            for (ConstraintViolation<ImportVehicleDto> violation : validator.validate(vehicle)) {
                violations.add(String.format("vehicle[%s].%s %s", i, violation.getPropertyPath(), violation.getMessage()));
            }
        }
        if (!violations.isEmpty()) {
            throw new ConstraintException(String.join(", ", violations));
        }
    }

    private List<ImportConflictInfo> detectConflicts(User user, List<ImportVehicleDto> vehicles) {
        List<ImportConflictInfo> conflicts = new ArrayList<>();
        for (int i = 0; i < vehicles.size(); i++) {
            ImportVehicleDto vehicleDto = vehicles.get(i);
            Optional<Coordinates> existingCoordinates = coordinatesDao.findByXY(
                    vehicleDto.getCoordinates().getX(),
                    vehicleDto.getCoordinates().getY()
            );
            if (existingCoordinates.isEmpty()) {
                continue;
            }
            if (vehicleDto.getId() != null) {
                Vehicle existingVehicle = vehicleDao.findById(vehicleDto.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Транспорт не найден"));
                enforceOwner(user, existingVehicle);
                if (existingVehicle.getCoordinates().getId() == existingCoordinates.get().getId()) {
                    continue;
                }
            }
            Integer existingVehicleId = vehicleDao.findFirstByCoordinatesId(existingCoordinates.get().getId())
                    .map(Vehicle::getId)
                    .orElse(null);
            conflicts.add(new ImportConflictInfo(
                    i,
                    existingVehicleId,
                    existingCoordinates.get().getX(),
                    existingCoordinates.get().getY()
            ));
        }
        return conflicts;
    }

    private void enforceOwner(User user, Vehicle vehicle) {
        if (user.getIsAdmin()) {
            return;
        }
        if (vehicle.getOwner().getId() != user.getId()) {
            throw new ForbiddenException("Доступ запрещен");
        }
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

    private void setRepeatableReadIsolation() {
        if (em == null) {
            return;
        }
        em.createNativeQuery("SET TRANSACTION ISOLATION LEVEL REPEATABLE READ").executeUpdate();
    }

    public record ImportConflictInfo(int vehicleIndex, Integer existingVehicleId, double coordinateX, Double coordinateY) {
    }

    public record ImportResult(int added, List<ImportConflictInfo> conflicts) {
    }
}
