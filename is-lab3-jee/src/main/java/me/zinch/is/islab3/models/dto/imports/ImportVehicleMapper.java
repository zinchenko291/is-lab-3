package me.zinch.is.islab3.models.dto.imports;

import jakarta.enterprise.context.ApplicationScoped;
import me.zinch.is.islab3.models.entities.Coordinates;
import me.zinch.is.islab3.models.entities.Vehicle;

@ApplicationScoped
public class ImportVehicleMapper {
    public Vehicle toEntity(ImportVehicleDto vehicleDto, Coordinates coordinates) {
        Vehicle vehicle = new Vehicle();
        applyToEntity(vehicle, vehicleDto, coordinates);
        return vehicle;
    }

    public void applyToEntity(Vehicle vehicle, ImportVehicleDto vehicleDto, Coordinates coordinates) {
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
}
