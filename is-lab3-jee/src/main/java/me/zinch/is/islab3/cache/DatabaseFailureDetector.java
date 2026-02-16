package me.zinch.is.islab3.cache;

import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public final class DatabaseFailureDetector {
    private static final long BACKOFF_MS = 15_000L;
    private static final AtomicLong UNAVAILABLE_UNTIL = new AtomicLong(0L);

    private DatabaseFailureDetector() {
    }

    public static void markFailure(Throwable ex) {
        if (isDbCommunicationFailure(ex)) {
            UNAVAILABLE_UNTIL.set(System.currentTimeMillis() + BACKOFF_MS);
        }
    }

    public static void markSuccess() {
        UNAVAILABLE_UNTIL.set(0L);
    }

    public static boolean isDatabaseLikelyDown() {
        return System.currentTimeMillis() < UNAVAILABLE_UNTIL.get();
    }

    public static boolean isDbCommunicationFailure(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof SQLException sqlException) {
                String state = sqlException.getSQLState();
                if (state != null && state.startsWith("08")) {
                    return true;
                }
                String message = lower(sqlException.getMessage());
                if (message.contains("connection") && (message.contains("closed")
                        || message.contains("refused")
                        || message.contains("reset")
                        || message.contains("timeout"))) {
                    return true;
                }
            }
            String message = lower(current.getMessage());
            if (message.contains("connection has been closed")
                    || message.contains("connection closed")
                    || message.contains("could not open connection")
                    || message.contains("failed to obtain/create connection")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
