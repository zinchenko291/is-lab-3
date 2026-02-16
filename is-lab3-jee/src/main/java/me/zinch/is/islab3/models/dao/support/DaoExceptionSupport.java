package me.zinch.is.islab3.models.dao.support;

import jakarta.persistence.PersistenceException;
import me.zinch.is.islab3.server.cache.DatabaseFailureDetector;
import me.zinch.is.islab3.exceptions.ConflictException;
import me.zinch.is.islab3.exceptions.ConstraintException;
import me.zinch.is.islab3.exceptions.StorageUnavailableException;
import me.zinch.is.islab3.models.fields.EntityField;
import me.zinch.is.islab3.models.fields.Filter;

import java.sql.SQLException;

public final class DaoExceptionSupport {
    private DaoExceptionSupport() {
    }

    public static RuntimeException mapPersistenceException(PersistenceException e, DatabaseFailureDetector databaseFailureDetector) {
        if (databaseFailureDetector.isDbCommunicationFailure(e)) {
            databaseFailureDetector.markFailure(e);
            return new StorageUnavailableException("Primary database is temporarily unavailable.", e);
        }
        if (hasSqlState(e, "23505")) {
            return new ConflictException("Unique constraint violation.");
        }
        if (hasSqlState(e, "23503")) {
            return new ConstraintException("Cannot delete resource because of references.");
        }
        if (hasSqlState(e, "40001")) {
            return new ConflictException("Transaction conflict. Please retry request.");
        }
        return e;
    }

    public static boolean isFieldConversionError(PersistenceException e, Filter<? extends EntityField> filter) {
        if (filter == null || filter.getValue() == null || filter.getValue().isEmpty()) {
            return false;
        }
        Throwable current = e;
        while (current != null) {
            if (current instanceof IllegalArgumentException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean hasSqlState(Throwable ex, String sqlState) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof SQLException && sqlState.equals(((SQLException) current).getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
