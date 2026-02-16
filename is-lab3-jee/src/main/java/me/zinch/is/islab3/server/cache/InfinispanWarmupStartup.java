package me.zinch.is.islab3.server.cache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class InfinispanWarmupStartup {
    public void onStartup(@Observes @Initialized(ApplicationScoped.class) Object event) {
        InfinispanL2CacheBridge.warmUp();
    }
}
