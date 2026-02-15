package me.zinch.is.islab2.server;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import me.zinch.is.islab2.models.entities.User;
import me.zinch.is.islab2.server.context.CurrentUser;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {
    private CurrentUser currentUser;

    public AuthFilter() {}

    @Inject
    public AuthFilter(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path.startsWith("auth") || "OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        User user = currentUser.getUser();
        if (user == null) {
            abortUnauthorized(requestContext);
        }
    }

    private void abortUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("Unauthorized")
                .build());
    }
}
