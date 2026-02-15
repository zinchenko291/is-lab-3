package me.zinch.is.islab2.server.exception_mappers;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ProcessingExceptionMapper implements ExceptionMapper<ProcessingException> {
    @Override
    public Response toResponse(ProcessingException e) {
        if (e.getCause() instanceof JsonbException) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.getCause().getMessage())
                    .build();
        }

        throw e;
    }
}
