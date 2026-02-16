package me.zinch.is.islab3.server.exception_mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

//@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
    @Override
    public Response toResponse(IllegalArgumentException e) {
        throw e;
    }
}
