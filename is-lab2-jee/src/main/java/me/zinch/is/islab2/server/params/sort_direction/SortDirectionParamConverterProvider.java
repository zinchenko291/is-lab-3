package me.zinch.is.islab2.server.params.sort_direction;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import me.zinch.is.islab2.models.fields.SortDirection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class SortDirectionParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> clazz, Type type, Annotation[] annotations) {
        if (clazz.equals(SortDirection.class)) {
            @SuppressWarnings("unchecked") // была выполнена проверка, что clazz это SortDirection
            ParamConverter<T> pc = (ParamConverter<T>) new SortDirectionConverter();
            return pc;
        }
        return null;
    }
}
