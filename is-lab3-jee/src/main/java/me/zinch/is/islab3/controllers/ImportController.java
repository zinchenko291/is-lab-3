package me.zinch.is.islab3.controllers;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.zinch.is.islab3.models.dto.imports.ImportConflictDto;
import me.zinch.is.islab3.models.dto.imports.ImportFailureModeToggleDto;
import me.zinch.is.islab3.models.dto.imports.ImportOperationDto;
import me.zinch.is.islab3.exceptions.ForbiddenException;
import me.zinch.is.islab3.models.entities.ImportConflictResolution;
import me.zinch.is.islab3.models.entities.ImportFormat;
import me.zinch.is.islab3.models.entities.User;
import me.zinch.is.islab3.server.context.CurrentUser;
import me.zinch.is.islab3.services.imports.ImportFailureMode;
import me.zinch.is.islab3.services.imports.ImportService;
import me.zinch.is.islab3.services.storage.S3StoredFile;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Path("imports")
@Produces(MediaType.APPLICATION_JSON)
public class ImportController {
    private final ImportService importService;
    private final CurrentUser currentUser;

    @Inject
    public ImportController(ImportService importService, CurrentUser currentUser) {
        this.importService = importService;
        this.currentUser = currentUser;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response importVehicles(@FormDataParam("file") InputStream file,
                                   @FormDataParam("file") FormDataContentDisposition details,
                                   @QueryParam("format") String format) {
        User user = currentUser.getUser();
        if (file == null || details == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Файл обязателен")
                    .build();
        }
        if (details.getSize() > 2 * 1024 * 1024) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Ограничение файла - 2 МБ")
                    .build();
        }
        ImportFormat importFormat = resolveFormat(format, details.getFileName());
        if (importFormat == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Формат должен быть yaml или xml")
                    .build();
        }
        try {
            byte[] content = file.readAllBytes();
            ImportOperationDto operation = importService.startImport(
                    user,
                    importFormat,
                    details.getFileName(),
                    MediaType.APPLICATION_OCTET_STREAM,
                    content
            );
            return Response.ok(operation).build();
        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Не удалось прочитать загрузку")
                    .build();
        }
    }

    @GET
    public Response getHistory() {
        User user = currentUser.getUser();
        List<ImportOperationDto> operations = importService.getHistoryForUser(user);
        return Response.ok(operations).build();
    }

    @GET
    @Path("{id}/conflicts")
    public Response getConflicts(@PathParam("id") Integer id) {
        User user = currentUser.getUser();
        List<ImportConflictDto> conflicts = importService.getConflictsForUser(user, id);
        return Response.ok(conflicts).build();
    }

    @POST
    @Path("{id}/conflicts/{conflictId}/resolve")
    public Response resolveConflict(@PathParam("id") Integer id,
                                    @PathParam("conflictId") Integer conflictId,
                                    @QueryParam("resolution") ImportConflictResolution resolution) {
        User user = currentUser.getUser();
        if (resolution == null || resolution == ImportConflictResolution.UNRESOLVED) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Решение должно быть SKIP или OVERWRITE")
                    .build();
        }
        ImportOperationDto operation = importService.resolveConflict(user, id, conflictId, resolution);
        return Response.ok(operation).build();
    }

    @GET
    @Path("{id}/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("id") Integer id) {
        User user = currentUser.getUser();
        S3StoredFile storedFile = importService.downloadFileForUser(user, id);
        String fileName = storedFile.fileName() == null ? "import.dat" : storedFile.fileName();
        String contentType = storedFile.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM : storedFile.contentType();
        return Response.ok(storedFile.content(), contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .header("Access-Control-Expose-Headers", HttpHeaders.CONTENT_DISPOSITION)
                .build();
    }

    @GET
    @Path("failure-mode")
    public Response getFailureMode() {
        User user = currentUser.getUser();
        if (!user.getIsAdmin()) {
            throw new ForbiddenException("Forbidden");
        }
        return Response.ok(importService.getFailureMode()).build();
    }

    @PUT
    @Path("failure-mode")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setFailureMode(ImportFailureModeToggleDto request) {
        User user = currentUser.getUser();
        if (!user.getIsAdmin()) {
            throw new ForbiddenException("Forbidden");
        }
        ImportFailureMode mode = request == null ? ImportFailureMode.NONE : request.getMode();
        importService.setFailureMode(mode == null ? ImportFailureMode.NONE : mode);
        return Response.ok(importService.getFailureMode()).build();
    }

    private ImportFormat resolveFormat(String formatParam, String filename) {
        String format = formatParam;
        if (format == null && filename != null) {
            int dot = filename.lastIndexOf('.');
            if (dot > -1 && dot < filename.length() - 1) {
                format = filename.substring(dot + 1);
            }
        }
        if (format == null) {
            return null;
        }
        String lower = format.toLowerCase();
        if (lower.equals("yaml") || lower.equals("yml")) {
            return ImportFormat.YAML;
        }
        if (lower.equals("xml")) {
            return ImportFormat.XML;
        }
        return null;
    }

}

