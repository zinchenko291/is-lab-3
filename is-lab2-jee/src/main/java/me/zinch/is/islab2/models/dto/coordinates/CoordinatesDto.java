package me.zinch.is.islab2.models.dto.coordinates;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import me.zinch.is.islab2.models.dto.user.UserShortDto;

public class CoordinatesDto {
    private int id;

    private double x;

    @NotNull
    @Max(910)
    private Double y;

    private UserShortDto owner;

    public CoordinatesDto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public @NotNull @Max(910) Double getY() {
        return y;
    }

    public void setY(@NotNull @Max(910) Double y) {
        this.y = y;
    }

    public UserShortDto getOwner() {
        return owner;
    }

    public void setOwner(UserShortDto owner) {
        this.owner = owner;
    }
}
