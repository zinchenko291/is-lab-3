package me.zinch.is.islab3.models.entities;

public enum ImportConflictResolution {
    UNRESOLVED("UNRESOLVED"),
    SKIP("SKIP"),
    OVERWRITE("OVERWRITE")
    ;

    private final String value;

    ImportConflictResolution(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
