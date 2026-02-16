package me.zinch.is.islab3.models.dto.vehicle;

import me.zinch.is.islab3.models.dto.coordinates.CoordinatesDto;
import me.zinch.is.islab3.models.entities.FuelType;
import me.zinch.is.islab3.models.entities.VehicleType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VehicleWithoutIdDto {
    @NotBlank
    private String name;

    @NotNull
    private CoordinatesDto coordinates;

    private VehicleType type;

    @Min(1)
    private int enginePower;

    @Min(1)
    private Long numberOfWheels;

    @Min(1)
    private Double capacity;

    @Min(1)
    private int distanceTravelled;

    @Min(1)
    private double fuelConsumption;

    @NotNull
    private FuelType fuelType;

    public @NotBlank String getName() {
        return name;
    }

    public void setName(@NotBlank String name) {
        this.name = name;
    }

    public @NotNull CoordinatesDto getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(@NotNull CoordinatesDto coordinates) {
        this.coordinates = coordinates;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    @Min(1)
    public int getEnginePower() {
        return enginePower;
    }

    public void setEnginePower(@Min(1) int enginePower) {
        this.enginePower = enginePower;
    }

    public @Min(1) Long getNumberOfWheels() {
        return numberOfWheels;
    }

    public void setNumberOfWheels(@Min(1) Long numberOfWheels) {
        this.numberOfWheels = numberOfWheels;
    }

    public @Min(1) Double getCapacity() {
        return capacity;
    }

    public void setCapacity(@Min(1) Double capacity) {
        this.capacity = capacity;
    }

    @Min(1)
    public int getDistanceTravelled() {
        return distanceTravelled;
    }

    public void setDistanceTravelled(@Min(1) int distanceTravelled) {
        this.distanceTravelled = distanceTravelled;
    }

    @Min(1)
    public double getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(@Min(1) double fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public @NotNull FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(@NotNull FuelType fuelType) {
        this.fuelType = fuelType;
    }
}
