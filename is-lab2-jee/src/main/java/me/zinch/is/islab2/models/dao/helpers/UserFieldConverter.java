package me.zinch.is.islab2.models.dao.helpers;

import jakarta.enterprise.context.ApplicationScoped;
import me.zinch.is.islab2.exceptions.FieldValueConvertException;
import me.zinch.is.islab2.models.dao.interfaces.IConverter;

@ApplicationScoped
public class UserFieldConverter implements IConverter {
    @Override
    public Object prepareField(String field, String value) {
        try {
            return switch (field) {
                case "id" -> Integer.parseInt(value);
                case "isAdmin" -> parseBoolean(value);
                default -> value;
            };
        } catch (NumberFormatException e) {
            throw new FieldValueConvertException("Invalid value for field: " + field);
        }
    }

    private boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new FieldValueConvertException("Invalid value for field: isAdmin");
    }
}
