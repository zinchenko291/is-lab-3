package me.zinch.is.islab3.controllers;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.zinch.is.islab3.server.cache.L2CacheStatsService;
import me.zinch.is.islab3.server.cache.L2CacheStatsToggleRequestDto;
import me.zinch.is.islab3.exceptions.ForbiddenException;
import me.zinch.is.islab3.server.context.CurrentUser;

@Path("cache/l2")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class L2CacheController {
    private final L2CacheStatsService statsService;
    private final CurrentUser currentUser;

    @Inject
    public L2CacheController(L2CacheStatsService statsService, CurrentUser currentUser) {
        this.statsService = statsService;
        this.currentUser = currentUser;
    }

    @GET
    @Path("stats")
    public Response getStats() {
        ensureAdmin();
        return Response.ok(statsService.snapshot()).build();
    }

    @PUT
    @Path("stats-logging")
    public Response setLogging(@Valid L2CacheStatsToggleRequestDto request) {
        ensureAdmin();
        statsService.setLoggingEnabled(request.isEnabled());
        return Response.ok(statsService.snapshot()).build();
    }

    private void ensureAdmin() {
        if (currentUser.getUser() == null || !currentUser.getUser().getIsAdmin()) {
            throw new ForbiddenException("Forbidden");
        }
    }
}
