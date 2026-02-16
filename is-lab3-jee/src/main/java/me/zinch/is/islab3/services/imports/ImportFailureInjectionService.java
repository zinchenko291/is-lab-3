package me.zinch.is.islab3.services.imports;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class ImportFailureInjectionService {
    private final AtomicReference<ImportFailureMode> mode = new AtomicReference<>(ImportFailureMode.NONE);

    public ImportFailureMode getMode() {
        return mode.get();
    }

    public void setMode(ImportFailureMode mode) {
        this.mode.set(mode == null ? ImportFailureMode.NONE : mode);
    }

    public void failIfConfigured(ImportFailureMode point) {
        if (mode.get() == point) {
            throw new RuntimeException("Injected failure at point: " + point);
        }
    }
}
