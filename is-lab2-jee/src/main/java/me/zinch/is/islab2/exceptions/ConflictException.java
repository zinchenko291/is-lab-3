package me.zinch.is.islab2.exceptions;

import jakarta.ws.rs.core.Response;

public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super(message, Response.Status.CONFLICT.getStatusCode());
    }
}
