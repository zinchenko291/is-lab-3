package me.zinch.is.islab2.controllers;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.zinch.is.islab2.models.dto.vehicle.VehicleDto;
import me.zinch.is.islab2.models.dto.vehicle.VehicleWithoutIdDto;
import me.zinch.is.islab2.models.entities.FuelType;
import me.zinch.is.islab2.models.entities.Vehicle;
import me.zinch.is.islab2.models.fields.Range;
import me.zinch.is.islab2.models.fields.SortDirection;
import me.zinch.is.islab2.models.fields.VehicleField;
import me.zinch.is.islab2.server.context.CurrentUser;
import me.zinch.is.islab2.services.VehicleService;

@Path("vehicles")
public class VehicleController extends AbstractController<Vehicle, VehicleField, VehicleDto, VehicleWithoutIdDto> {
    private final VehicleService vehicleService;
    private final CurrentUser currentUser;

    @Inject
    public VehicleController(VehicleService vehicleService, CurrentUser currentUser) {
        super(vehicleService);
        this.vehicleService = vehicleService;
        this.currentUser = currentUser;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMany(
            @QueryParam("field") VehicleField field,
            @QueryParam("value") String value,
            @QueryParam("orderBy") SortDirection orderBy,
            @QueryParam("limit") @Min(0) @DefaultValue("50") Integer limit,
            @QueryParam("offset") @Min(0) @DefaultValue("0") Integer offset
    ) {
        return Response
                .ok(vehicleService.findAllForUser(currentUser.getUser(), field, value, orderBy, limit, offset))
                .build();
    }

    @GET
    @Path("min-engine-power")
    public Response findMinEnginePower() {
        return Response
                .ok(vehicleService.findMinEnginePowerForUser(currentUser.getUser()))
                .build();
    }

    @GET
    @Path("count-gt-fuel-type")
public Response countGtFuelType(@QueryParam("fuelType") FuelType fuelType) {
        if (fuelType == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("fuelType is required")
                    .build();
        }

        return Response.ok(vehicleService.countGtFuelTypeForUser(currentUser.getUser(), fuelType)).build();
    }

    @GET
    @Path("search-by-name")
    public Response findByNameSubstring(
            @QueryParam("name") @NotBlank String name,
            @QueryParam("offset") @Min(0) @DefaultValue("1") Integer offset,
            @QueryParam("limit") @Min(0) @DefaultValue("50") Integer limit
    ) {
        return Response
                .ok(vehicleService.findByNameSubstringForUser(currentUser.getUser(), offset, limit, name))
                .build();
    }

    @GET
    @Path("engine-power-range")
    public Response findByEnginePowerRange(
            @QueryParam("min") @NotNull Integer min,
            @QueryParam("max") @NotNull Integer max,
            @QueryParam("offset") @Min(0) @DefaultValue("1") Integer offset,
            @QueryParam("limit") @Min(0) @DefaultValue("50") Integer limit
    ) {
        Range<Integer> range = new Range<>(min, max);
        return Response.
                ok(vehicleService.findByEnginePowerRangeForUser(currentUser.getUser(), offset, limit, range))
                .build();
    }

    @POST
    @Path("{id}/reset-distance")
    public Response resetDistanceTravelled(@PathParam("id") Integer id) {
        return Response
                .ok(vehicleService.resetDistanceTravelledByIdForUser(currentUser.getUser(), id))
                .build();
    }

    @Override
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") Integer id) {
        return Response.ok(vehicleService.findByIdForUser(currentUser.getUser(), id))
                .build();
    }

    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Valid VehicleWithoutIdDto dto) {
        return Response.status(201)
                .entity(vehicleService.createForUser(currentUser.getUser(), dto))
                .build();
    }

    @Override
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Integer id, @Valid VehicleDto dto) {
        return Response.ok()
                .entity(vehicleService.updateByIdForUser(currentUser.getUser(), id, dto))
                .build();
    }

    @Override
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Integer id) {
        return Response.ok()
                .entity(vehicleService.deleteByIdForUser(currentUser.getUser(), id))
                .build();
    }
}
