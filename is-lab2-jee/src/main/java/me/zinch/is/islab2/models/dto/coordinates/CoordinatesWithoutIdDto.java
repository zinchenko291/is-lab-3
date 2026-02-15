package me.zinch.is.islab2.models.dto.coordinates;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public class CoordinatesWithoutIdDto {
    private double x;

    @NotNull
    @Max(910)
    private Double y;

    public CoordinatesWithoutIdDto() {
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
}
