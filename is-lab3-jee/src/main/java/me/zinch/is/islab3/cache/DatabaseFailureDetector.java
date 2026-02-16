package me.zinch.is.islab3.cache;

import jakarta.enterprise.context.ApplicationScoped;

import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.SQLTransientConnectionException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class DatabaseFailureDetector {
    private static final long BACKOFF_MS = 3_000L;
    private static final Set<String> SQL_STATE_PREFIXES = Set.of("08", "53");
    private static final Set<String> SQL_STATE_EXACT = Set.of("57P01", "57P02", "57P03");
    private static final String[] CONNECTION_MESSAGE_MARKERS = {
            "closed",
            "disabled",
            "aborted",
            "refused",
            "reset",
            "broken pipe",
            "timeout"
    };
    private static final String[] GENERIC_MESSAGE_MARKERS = {
            "i/o error",
            "socket closed",
            "network is unreachable",
            "no route to host",
            "while sending to the backend",
            "communication link failure"
    };
    private static final String[] TOP_LEVEL_MESSAGE_MARKERS = {
            "connection has been closed",
            "connection closed",
            "connection disabled",
            "an established connection was aborted",
            "software caused connection abort",
            "communication failure detected",
            "an i/o error occurred while sending to the backend",
            "while sending to the backend",
            "socket closed",
            "could not open connection",
            "failed to obtain/create connection"
    };

    private final AtomicLong unavailableUntil = new AtomicLong(0L);

    public void markFailure(Throwable ex) {
        if (isDbCommunicationFailure(ex)) {
            unavailableUntil.set(System.currentTimeMillis() + BACKOFF_MS);
        }
    }

    public void markSuccess() {
        unavailableUntil.set(0L);
    }

    public boolean isDatabaseLikelyDown() {
        return System.currentTimeMillis() < unavailableUntil.get();
    }

    public boolean isDbCommunicationFailure(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof SQLRecoverableException || current instanceof SQLTransientConnectionException) {
                return true;
            }
            if (current instanceof SQLException sqlException) {
                String state = sqlException.getSQLState();
                if (isCommunicationSqlState(state)) {
                    return true;
                }
                String message = lower(sqlException.getMessage());
                if ((message.contains("connection") && containsAny(message, CONNECTION_MESSAGE_MARKERS))
                        || containsAny(message, GENERIC_MESSAGE_MARKERS)) {
                    return true;
                }
            }
            String message = lower(current.getMessage());
            if (containsAny(message, TOP_LEVEL_MESSAGE_MARKERS)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isCommunicationSqlState(String state) {
        if (state == null) {
            return false;
        }
        if (SQL_STATE_EXACT.contains(state)) {
            return true;
        }
        for (String prefix : SQL_STATE_PREFIXES) {
            if (state.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAny(String message, String[] markers) {
        for (String marker : markers) {
            if (message.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
