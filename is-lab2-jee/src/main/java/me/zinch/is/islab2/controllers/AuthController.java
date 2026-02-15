package me.zinch.is.islab2.controllers;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.zinch.is.islab2.models.dto.auth.*;
import me.zinch.is.islab2.models.dto.user.UserDto;
import me.zinch.is.islab2.models.dto.user.UserMapper;
import me.zinch.is.islab2.server.context.CurrentUser;
import me.zinch.is.islab2.services.AuthService;

import java.util.Optional;

@Path("auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {
    private final AuthService authService;
    private final CurrentUser currentUser;
    private final UserMapper userMapper;

    @Inject
    public AuthController(AuthService authService, CurrentUser currentUser, UserMapper userMapper) {
        this.authService = authService;
        this.currentUser = currentUser;
        this.userMapper = userMapper;
    }

    @POST
    @Path("register")
    public Response startRegistration(@Valid RegisterRequestDto request) {
        String challenge = authService.startRegistration(request.getName(), request.getPubkey(), request.getEmail());
        return Response.ok(new ChallengeResponseDto(challenge)).build();
    }

    @POST
    @Path("register/verify")
    public Response finishRegistration(@Valid RegisterVerifyRequestDto request) {
        UserDto user = authService.finishRegistration(request.getPubkey(), request.getSignature());
        currentUser.setUser(userMapper.dtoToEntity(user));
        return Response.ok(user).build();
    }

    @POST
    @Path("login")
    public Response startLogin(@Valid LoginRequestDto request) {
        String challenge = authService.startLogin(request.getPubkey());
        return Response.ok(new ChallengeResponseDto(challenge)).build();
    }

    @POST
    @Path("login/verify")
    public Response finishLogin(@Valid LoginVerifyRequestDto request) {
        UserDto user = authService.finishLogin(request.getPubkey(), request.getSignature());
        currentUser.setUser(userMapper.dtoToEntity(user));
        return Response.ok(user).build();
    }

    @GET
    @Path("validate")
    public Response validateLogin() {
        return Response.status(currentUser.getUser() == null ? Response.Status.UNAUTHORIZED : Response.Status.OK).build();
    }

    @GET
    @Path("me")
    public Response getMe() {
        return Optional.ofNullable(currentUser.getUser())
                .map(user -> Response.ok(user).build())
                .orElse(Response.status(Response.Status.UNAUTHORIZED).build());
    }

    @GET
    @Path("logout")
    public Response logout() {
        currentUser.setUser(null);
        return Response.noContent().build();
    }
}
