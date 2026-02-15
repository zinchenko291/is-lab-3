package me.zinch.is.islab2.models.dto.vehicle;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import me.zinch.is.islab2.models.dto.IMapper;
import me.zinch.is.islab2.models.dto.coordinates.CoordinatesMapper;
import me.zinch.is.islab2.models.dto.user.UserMapper;
import me.zinch.is.islab2.models.entities.Vehicle;


@ApplicationScoped
public class VehicleMapper implements IMapper<Vehicle, VehicleDto, VehicleWithoutIdDto> {
    private CoordinatesMapper coordinatesMapper;
    private UserMapper userMapper;

    public VehicleMapper() {}

    @Inject
    public VehicleMapper(CoordinatesMapper coordinatesMapper, UserMapper userMapper) {
        this.coordinatesMapper = coordinatesMapper;
        this.userMapper = userMapper;
    }

    @Override
    public VehicleDto entityToDto(Vehicle vehicle) {
        if (vehicle == null) return null;
        VehicleDto vehicleDto = null;
        vehicleDto = new VehicleDto();
        vehicleDto.setId(vehicle.getId());
        vehicleDto.setName(vehicle.getName());
        vehicleDto.setCoordinates(coordinatesMapper.entityToDto(vehicle.getCoordinates()));
        vehicleDto.setCreationDate(vehicle.getCreationDate());
        vehicleDto.setType(vehicle.getType());
        vehicleDto.setEnginePower(vehicle.getEnginePower());
        vehicleDto.setNumberOfWheels(vehicle.getNumberOfWheels());
        vehicleDto.setCapacity(vehicle.getCapacity());
        vehicleDto.setDistanceTravelled(vehicle.getDistanceTravelled());
        vehicleDto.setFuelConsumption(vehicle.getFuelConsumption());
        vehicleDto.setFuelType(vehicle.getFuelType());
        vehicleDto.setOwner(userMapper == null ? null : userMapper.entityToShortDto(vehicle.getOwner()));
        return vehicleDto;
    }

    @Override
    public Vehicle idLessDtoToEntity(VehicleWithoutIdDto vehicleDto) {
        if (vehicleDto == null) return null;
        Vehicle vehicle = new Vehicle();
        vehicle.setName(vehicleDto.getName());
        vehicle.setCoordinates(coordinatesMapper.dtoToEntity(vehicleDto.getCoordinates()));
        vehicle.setType(vehicleDto.getType());
        vehicle.setEnginePower(vehicleDto.getEnginePower());
        vehicle.setNumberOfWheels(vehicleDto.getNumberOfWheels());
        vehicle.setCapacity(vehicleDto.getCapacity());
        vehicle.setDistanceTravelled(vehicleDto.getDistanceTravelled());
        vehicle.setFuelConsumption(vehicleDto.getFuelConsumption());
        vehicle.setFuelType(vehicleDto.getFuelType());
        return vehicle;
    }

    @Override
    public Vehicle dtoToEntity(VehicleDto vehicleDto) {
        if (vehicleDto == null) return null;
        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleDto.getId());
        vehicle.setName(vehicleDto.getName());
        vehicle.setCoordinates(coordinatesMapper.dtoToEntity(vehicleDto.getCoordinates()));
        vehicle.setCreationDate(vehicleDto.getCreationDate());
        vehicle.setType(vehicleDto.getType());
        vehicle.setEnginePower(vehicleDto.getEnginePower());
        vehicle.setNumberOfWheels(vehicleDto.getNumberOfWheels());
        vehicle.setCapacity(vehicleDto.getCapacity());
        vehicle.setDistanceTravelled(vehicleDto.getDistanceTravelled());
        vehicle.setFuelConsumption(vehicleDto.getFuelConsumption());
        vehicle.setFuelType(vehicleDto.getFuelType());
        return vehicle;
    }
}
