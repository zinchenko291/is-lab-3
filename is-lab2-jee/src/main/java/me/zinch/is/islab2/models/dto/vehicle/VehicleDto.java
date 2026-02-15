package me.zinch.is.islab2.models.dto.vehicle;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.zinch.is.islab2.models.dto.coordinates.CoordinatesDto;
import me.zinch.is.islab2.models.dto.user.UserShortDto;
import me.zinch.is.islab2.models.entities.FuelType;
import me.zinch.is.islab2.models.entities.VehicleType;
import java.util.Date;

public class VehicleDto {
    private int id;

    @NotBlank
    private String name;

    @NotNull
    private CoordinatesDto coordinates;

    private Date creationDate;

    @Enumerated(EnumType.STRING)
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

    private UserShortDto owner;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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

    public UserShortDto getOwner() {
        return owner;
    }

    public void setOwner(UserShortDto owner) {
        this.owner = owner;
    }
}
