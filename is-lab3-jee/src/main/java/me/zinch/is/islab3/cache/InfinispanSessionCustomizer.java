package me.zinch.is.islab3.cache;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.sessions.Session;

public class InfinispanSessionCustomizer implements SessionCustomizer {
    @Override
    public void customize(Session session) {
        for (ClassDescriptor descriptor : session.getDescriptors().values()) {
            descriptor.setCacheInterceptorClass(InfinispanEclipseLinkCacheInterceptor.class);
        }
    }
}
