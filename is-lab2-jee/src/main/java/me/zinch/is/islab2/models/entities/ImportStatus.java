package me.zinch.is.islab2.models.entities;

public enum ImportStatus {
    PENDING("PENDING"),
    RUNNING("RUNNING"),
    PAUSED("PAUSED"),
    FAILED("FAILED"),
    SUCCEEDED("SUCCEEDED")
    ;

    private final String value;

    ImportStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
