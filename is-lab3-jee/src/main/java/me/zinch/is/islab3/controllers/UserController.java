package me.zinch.is.islab3.controllers;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.zinch.is.islab3.models.dto.user.UserDto;
import me.zinch.is.islab3.models.dto.user.UserWithoutIdDto;
import me.zinch.is.islab3.models.entities.User;
import me.zinch.is.islab3.models.fields.SortDirection;
import me.zinch.is.islab3.models.fields.UserField;
import me.zinch.is.islab3.services.UserService;

@Path("users")
public class UserController extends AbstractController<User, UserField, UserDto, UserWithoutIdDto> {
    @Inject
    public UserController(UserService service) {
        super(service);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(
            @QueryParam("field") UserField field,
            @QueryParam("value") String value,
            @QueryParam("orderBy") SortDirection orderBy,
            @QueryParam("limit") @Min(0) @DefaultValue("50") Integer limit,
            @QueryParam("offset") @Min(0) @DefaultValue("0") Integer offset
    ) {
        return Response
                .ok(service.findAll(field, value, orderBy, limit, offset))
                .build();
    }
}
