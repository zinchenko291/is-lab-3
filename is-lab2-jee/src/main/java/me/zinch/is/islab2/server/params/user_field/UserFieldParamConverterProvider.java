package me.zinch.is.islab2.server.params.user_field;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import me.zinch.is.islab2.models.fields.UserField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class UserFieldParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> clazz, Type type, Annotation[] annotations) {
        if (clazz.equals(UserField.class)) {
            @SuppressWarnings("unchecked")
            ParamConverter<T> pc = (ParamConverter<T>) new UserFieldConverter();
            return pc;
        }
        return null;
    }
}
