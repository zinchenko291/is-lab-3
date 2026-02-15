package me.zinch.is.islab2.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @ManyToOne
    @JoinColumn(name = "coordinateId")
    @NotNull
    private Coordinates coordinates;

    @ManyToOne
    @JoinColumn(name = "ownerId", nullable = false)
    @NotNull
    private User owner;

    @Column(nullable = false, insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column
    @Enumerated(EnumType.STRING)
    private VehicleType type;

    @Column(nullable = false)
    @Min(1)
    private int enginePower;

    @Column
    @Min(1)
    private Long numberOfWheels;

    @Column
    @Min(1)
    private Double capacity;

    @Column
    @Min(1)
    private int distanceTravelled;

    @Column
    @Min(1)
    private double fuelConsumption;

    @Column
    @Enumerated(EnumType.STRING)
    @NotNull
    private FuelType fuelType;

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

    public @NotNull Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(@NotNull Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public @NotNull User getOwner() {
        return owner;
    }

    public void setOwner(@NotNull User owner) {
        this.owner = owner;
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
}
