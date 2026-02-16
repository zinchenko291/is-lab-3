package me.zinch.is.islab3.server.params.import_conflict_resolution;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import me.zinch.is.islab3.models.entities.ImportConflictResolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class ImportConflictResolutionParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> clazz, Type type, Annotation[] annotations) {
        if (clazz.equals(ImportConflictResolution.class)) {
            @SuppressWarnings("unchecked")
            ParamConverter<T> converter = (ParamConverter<T>) new ImportConflictResolutionConverter();
            return converter;
        }
        return null;
    }
}
