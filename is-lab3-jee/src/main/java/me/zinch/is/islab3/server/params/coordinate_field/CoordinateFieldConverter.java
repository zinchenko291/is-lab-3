package me.zinch.is.islab3.server.params.coordinate_field;

import me.zinch.is.islab3.exceptions.DeserializingException;
import me.zinch.is.islab3.models.fields.CoordinatesField;

import jakarta.ws.rs.ext.ParamConverter;

public class CoordinateFieldConverter implements ParamConverter<CoordinatesField> {
    @Override
    public CoordinatesField fromString(String s) {
        try {
            return new CoordinatesField(s);
        } catch (IllegalArgumentException e) {
            throw new DeserializingException(
                    String.format("Поле %s отсутствует в сущности Coordinate", s)
            );
        }
    }

    @Override
    public String toString(CoordinatesField coordinatesField) {
        return coordinatesField.toString();
    }
}
