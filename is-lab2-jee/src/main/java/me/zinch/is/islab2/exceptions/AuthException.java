package me.zinch.is.islab2.exceptions;

import jakarta.ws.rs.core.Response;

public class AuthException extends BusinessException {
    public AuthException(String message) {
        super(message, Response.Status.UNAUTHORIZED.getStatusCode());
    }
}
