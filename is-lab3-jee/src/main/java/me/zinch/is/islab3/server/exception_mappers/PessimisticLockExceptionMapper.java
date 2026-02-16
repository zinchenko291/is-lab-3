package me.zinch.is.islab3.server.exception_mappers;

import jakarta.persistence.PessimisticLockException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PessimisticLockExceptionMapper implements ExceptionMapper<PessimisticLockException> {
    @Override
    public Response toResponse(PessimisticLockException e) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.TEXT_PLAIN)
                .entity("Конфликт транзакции. Повторите запрос.")
                .build();
    }
}
