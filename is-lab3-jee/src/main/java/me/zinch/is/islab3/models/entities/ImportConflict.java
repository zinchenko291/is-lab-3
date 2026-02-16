package me.zinch.is.islab3.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import me.zinch.is.islab3.cache.InfinispanEclipseLinkCacheInterceptor;
import org.eclipse.persistence.annotations.CacheInterceptor;

import java.util.Date;

@Entity
@CacheInterceptor(InfinispanEclipseLinkCacheInterceptor.class)
@Table(name = "import_conflicts")
public class ImportConflict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "operationId", nullable = false)
    @NotNull
    private ImportOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private ImportConflictResolution resolution;

    @Column(nullable = false)
    private int vehicleIndex;

    @Column
    private Integer existingVehicleId;

    @Column(nullable = false)
    private double coordinateX;

    @Column(nullable = false)
    @NotNull
    private Double coordinateY;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public @NotNull ImportOperation getOperation() {
        return operation;
    }

    public void setOperation(@NotNull ImportOperation operation) {
        this.operation = operation;
    }

    public @NotNull ImportConflictResolution getResolution() {
        return resolution;
    }

    public void setResolution(@NotNull ImportConflictResolution resolution) {
        this.resolution = resolution;
    }

    public int getVehicleIndex() {
        return vehicleIndex;
    }

    public void setVehicleIndex(int vehicleIndex) {
        this.vehicleIndex = vehicleIndex;
    }

    public Integer getExistingVehicleId() {
        return existingVehicleId;
    }

    public void setExistingVehicleId(Integer existingVehicleId) {
        this.existingVehicleId = existingVehicleId;
    }

    public double getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(double coordinateX) {
        this.coordinateX = coordinateX;
    }

    public @NotNull Double getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(@NotNull Double coordinateY) {
        this.coordinateY = coordinateY;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
