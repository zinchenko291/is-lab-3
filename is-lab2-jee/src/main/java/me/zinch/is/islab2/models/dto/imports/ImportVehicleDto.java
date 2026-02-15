package me.zinch.is.islab2.models.dto.imports;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.zinch.is.islab2.models.entities.FuelType;
import me.zinch.is.islab2.models.entities.VehicleType;

public class ImportVehicleDto {
    private Integer id;

    @NotBlank
    private String name;

    @NotNull
    @Valid
    private ImportCoordinatesDto coordinates;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public @NotBlank String getName() {
        return name;
    }

    public void setName(@NotBlank String name) {
        this.name = name;
    }

    public @NotNull @Valid ImportCoordinatesDto getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(@NotNull @Valid ImportCoordinatesDto coordinates) {
        this.coordinates = coordinates;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public @Min(1) int getEnginePower() {
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

    public @Min(1) int getDistanceTravelled() {
        return distanceTravelled;
    }

    public void setDistanceTravelled(@Min(1) int distanceTravelled) {
        this.distanceTravelled = distanceTravelled;
    }

    public @Min(1) double getFuelConsumption() {
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
