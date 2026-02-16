package me.zinch.is.islab3.server.exception_mappers;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException e) {
        return Response
                .status(400)
                .entity(
                        e.getConstraintViolations()
                        .stream()
                        .map(v -> String.format("%s: %s", v.getPropertyPath(), v.getMessage()))
                        .collect(Collectors.joining(", "))
                )
                .build();
    }
}
