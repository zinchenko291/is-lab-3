package me.zinch.is.islab3.server.exception_mappers;

import me.zinch.is.islab3.exceptions.DeserializingException;
import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.server.ParamException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ParamExceptionMapper implements ExceptionMapper<ParamException> {
    @Override
    public Response toResponse(ParamException e) {
        if (e.getCause() instanceof DeserializingException) {
            return Response
                    .status(400)
                    .entity(e.getCause().getMessage())
                    .build();
        }

        if (e.getCause() instanceof ExtractorException
                && e.getCause().getCause() instanceof NumberFormatException) {
            return Response
                    .status(400)
                    .entity(String.format("Невозможно представить %s как число", e.getCause().getCause().getMessage()))
                    .build();
        }

        if (e.getCause() instanceof ExtractorException
                && e.getCause().getCause() instanceof IllegalArgumentException) {
            return Response
                    .status(400)
                    .entity(e.getCause().getCause().getMessage())
                    .build();
        }

        throw e;
    }
}
