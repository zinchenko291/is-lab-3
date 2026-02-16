package me.zinch.is.islab3.exceptions;

import jakarta.ws.rs.core.Response;

public class StorageUnavailableException extends BusinessException {
    public StorageUnavailableException(String message) {
        super(message, Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    public StorageUnavailableException(String message, Throwable cause) {
        super(message, Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), cause);
    }
}
