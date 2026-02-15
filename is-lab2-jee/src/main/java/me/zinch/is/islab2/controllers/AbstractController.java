package me.zinch.is.islab2.controllers;

import jakarta.validation.Valid;
import me.zinch.is.islab2.models.fields.EntityField;
import me.zinch.is.islab2.services.AbstractService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public abstract class AbstractController<E, F extends EntityField, D, I> {
    protected final AbstractService<E, F, D, I> service;

    protected AbstractController(AbstractService<E, F, D, I> service) {
        this.service = service;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
            @PathParam("id") Integer id
    ) {
        return Response.ok(service.findById(id))
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Valid I dto) {
        return Response.status(201)
                .entity(service.create(dto))
                .build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") Integer id,
            @Valid D dto
    ) {
        return Response.ok()
                .entity(service.updateById(id, dto))
                .build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") Integer id
    ) {
        return Response.ok()
                .entity(service.deleteById(id))
                .build();
    }
}
