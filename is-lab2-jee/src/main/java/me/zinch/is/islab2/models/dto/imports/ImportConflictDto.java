package me.zinch.is.islab2.models.dto.imports;

import me.zinch.is.islab2.models.entities.ImportConflictResolution;

import java.util.Date;

public class ImportConflictDto {
    private int id;
    private ImportConflictResolution resolution;
    private int vehicleIndex;
    private Integer existingVehicleId;
    private double coordinateX;
    private Double coordinateY;
    private Integer userId;
    private Date createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ImportConflictResolution getResolution() {
        return resolution;
    }

    public void setResolution(ImportConflictResolution resolution) {
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

    public Double getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(Double coordinateY) {
        this.coordinateY = coordinateY;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
