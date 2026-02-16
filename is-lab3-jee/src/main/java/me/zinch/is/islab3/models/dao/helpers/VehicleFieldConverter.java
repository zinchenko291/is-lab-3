package me.zinch.is.islab3.models.dao.helpers;

import jakarta.enterprise.context.ApplicationScoped;
import me.zinch.is.islab3.exceptions.FieldValueConvertException;
import me.zinch.is.islab3.models.dao.interfaces.IConverter;
import me.zinch.is.islab3.models.entities.FuelType;
import me.zinch.is.islab3.models.entities.VehicleType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class VehicleFieldConverter implements IConverter {
    public Object prepareField(String field, String value) {
        return switch (field) {
            case "type" -> prepareEnumField(VehicleType.class, value);
            case "fuelType" -> prepareEnumField(FuelType.class, value);
            case "creationDate" -> dateFormat(value);
            default -> value;
        };
    }

    private <T extends Enum<T>> T prepareEnumField(Class<T> enumClass, String value) {
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            throw new FieldValueConvertException("Не удалось преобразовать поле в разрешённое значение");
        }
    }

    private String dateFormat(String date) {
        Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})T(\\d{2}:\\d{2}:\\d{2}(?:.\\d{3})?)Z?");
        Matcher matcher = pattern.matcher(date);
        if (matcher.matches()) {
            return String.format("%s %s", matcher.group(1), matcher.group(2));
        }
        throw new FieldValueConvertException("Не удалось преобразовать дату");
    }
}
