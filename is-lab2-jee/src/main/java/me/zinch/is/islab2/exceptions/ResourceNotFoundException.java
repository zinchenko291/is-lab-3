package me.zinch.is.islab2.exceptions;

import jakarta.ws.rs.core.Response;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message, Response.Status.NOT_FOUND.getStatusCode());
    }
}
