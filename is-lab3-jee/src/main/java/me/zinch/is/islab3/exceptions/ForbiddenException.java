package me.zinch.is.islab3.exceptions;

import jakarta.ws.rs.core.Response;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message) {
        super(message, Response.Status.FORBIDDEN.getStatusCode());
    }
}
