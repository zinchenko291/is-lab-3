package me.zinch.is.islab3;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api")
public class IsApplication extends ResourceConfig {
    public IsApplication() {
        packages("me.zinch.is.islab3");
        register(MultiPartFeature.class);
    }
}
