package me.zinch.is.islab3.server.params.sort_direction;

import me.zinch.is.islab3.exceptions.DeserializingException;
import me.zinch.is.islab3.models.fields.SortDirection;

import jakarta.ws.rs.ext.ParamConverter;

public class SortDirectionConverter implements ParamConverter<SortDirection> {
    @Override
    public SortDirection fromString(String str) {
        try {
            return new SortDirection(str);
        } catch (IllegalArgumentException e) {
            throw new DeserializingException(
                String.format("Не удалось распознать значение %s. Параметр сортировки принимает значения только ASC или DESC", str)
            );
        }
    }

    @Override
    public String toString(SortDirection sortDirection) {
        return sortDirection.getValue();
    }
}
