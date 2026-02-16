package me.zinch.is.islab3.models.dto.imports;

import me.zinch.is.islab3.services.imports.ImportFailureMode;

public class ImportFailureModeToggleDto {
    private ImportFailureMode mode;

    public ImportFailureMode getMode() {
        return mode;
    }

    public void setMode(ImportFailureMode mode) {
        this.mode = mode;
    }
}
