package me.zinch.is.islab3.server.params.import_conflict_resolution;

import jakarta.ws.rs.ext.ParamConverter;
import me.zinch.is.islab3.exceptions.DeserializingException;
import me.zinch.is.islab3.models.entities.ImportConflictResolution;

public class ImportConflictResolutionConverter implements ParamConverter<ImportConflictResolution> {
    @Override
    public ImportConflictResolution fromString(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return ImportConflictResolution.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DeserializingException("Некорректное значение resolution");
        }
    }

    @Override
    public String toString(ImportConflictResolution value) {
        return value == null ? null : value.name();
    }
}
