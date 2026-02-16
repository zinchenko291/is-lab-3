package me.zinch.is.islab3.server.params.coordinate_field;

import me.zinch.is.islab3.models.fields.CoordinatesField;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class CoordinateFieldParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> clazz, Type type, Annotation[] annotations) {
        if (clazz.equals(CoordinatesField.class)) {
            @SuppressWarnings("unchecked") // была выполнена проверка, что clazz это CoordinateField
            ParamConverter<T> pc = (ParamConverter<T>) new CoordinateFieldConverter();
            return pc;
        }
        return null;
    }
}
