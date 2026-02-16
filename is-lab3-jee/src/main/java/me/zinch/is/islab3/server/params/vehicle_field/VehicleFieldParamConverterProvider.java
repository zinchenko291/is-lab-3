package me.zinch.is.islab3.server.params.vehicle_field;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import me.zinch.is.islab3.models.fields.VehicleField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class VehicleFieldParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> clazz, Type type, Annotation[] annotations) {
        if (clazz.equals(VehicleField.class)) {
            @SuppressWarnings("unchecked") // была выполнена проверка, что clazz это VehicleField
            ParamConverter<T> pc = (ParamConverter<T>) new VehicleFieldConverter();
            return pc;
        }
        return null;
    }
}
