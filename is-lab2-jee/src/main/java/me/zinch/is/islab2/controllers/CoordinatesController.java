package me.zinch.is.islab2.controllers;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.zinch.is.islab2.exceptions.AuthException;
import me.zinch.is.islab2.models.dto.coordinates.CoordinatesDto;
import me.zinch.is.islab2.models.dto.coordinates.CoordinatesWithoutIdDto;
import me.zinch.is.islab2.models.entities.Coordinates;
import me.zinch.is.islab2.models.entities.User;
import me.zinch.is.islab2.models.fields.CoordinatesField;
import me.zinch.is.islab2.models.fields.SortDirection;
import me.zinch.is.islab2.server.context.CurrentUser;
import me.zinch.is.islab2.services.CoordinatesService;

@Path("coordinates")
public class CoordinatesController extends AbstractController<Coordinates, CoordinatesField, CoordinatesDto, CoordinatesWithoutIdDto> {
    private final CoordinatesService coordinatesService;
    private final CurrentUser currentUser;

    @Inject
    public CoordinatesController(CoordinatesService service, CurrentUser currentUser) {
        super(service);
        this.coordinatesService = service;
        this.currentUser = currentUser;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCoordinates(
            @QueryParam("field") CoordinatesField field,
            @QueryParam("value") String value,
            @QueryParam("orderBy") SortDirection orderBy,
            @QueryParam("limit") @Min(0) @DefaultValue("50") Integer limit,
            @QueryParam("offset") @Min(0) @DefaultValue("0") Integer offset
            ) {
        return Response
                .ok(coordinatesService.findAllForUser(getCurrentUser(), field, value, orderBy, limit, offset))
            .build();
    }

    @Override
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") Integer id) {
        return Response.ok(coordinatesService.findByIdForUser(getCurrentUser(), id))
                .build();
    }

    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Valid CoordinatesWithoutIdDto dto) {
        return Response.status(201)
                .entity(coordinatesService.createForUser(getCurrentUser(), dto))
                .build();
    }

    @Override
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Integer id, @Valid CoordinatesDto dto) {
        return Response.ok()
                .entity(coordinatesService.updateByIdForUser(getCurrentUser(), id, dto))
                .build();
    }

    @Override
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Integer id) {
        return Response.ok()
                .entity(coordinatesService.deleteByIdForUser(getCurrentUser(), id))
                .build();
    }

    private User getCurrentUser() {
        User user = currentUser.getUser();
        if (user == null) {
            throw new AuthException("Unauthorized");
        }
        return user;
    }
}
