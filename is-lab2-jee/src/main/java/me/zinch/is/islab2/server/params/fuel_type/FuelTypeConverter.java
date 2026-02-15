package me.zinch.is.islab2.server.params.fuel_type;

import jakarta.ws.rs.ext.ParamConverter;
import me.zinch.is.islab2.exceptions.DeserializingException;
import me.zinch.is.islab2.models.entities.FuelType;

public class FuelTypeConverter implements ParamConverter<FuelType> {
    @Override
    public FuelType fromString(String s) {
        try {
            return FuelType.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw new DeserializingException(
                    String.format("Поля %s не существует в сущности Vehicle", s)
            );
        }
    }

    @Override
    public String toString(FuelType vehicleField) {
        return vehicleField.getValue();
    }
}
