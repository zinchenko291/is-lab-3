package me.zinch.is.islab2.server.params.fuel_type;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import me.zinch.is.islab2.models.entities.FuelType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class FuelTypeParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> clazz, Type type, Annotation[] annotations) {
        if (clazz.equals(FuelType.class)) {
            @SuppressWarnings("unchecked") // была выполнена проверка, что clazz это FuelType
            ParamConverter<T> pc = (ParamConverter<T>) new FuelTypeConverter();
            return pc;
        }
        return null;
    }
}
