package me.zinch.is.islab3.exceptions;

import jakarta.ws.rs.core.Response;

public class FieldValueConvertException extends BusinessException {
    public FieldValueConvertException(String message) {
        super(message, Response.Status.BAD_REQUEST.getStatusCode());
    }
}
