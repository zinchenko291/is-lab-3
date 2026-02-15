package me.zinch.is.islab2.models.ws;

public enum WsEntity {
    VEHICLE("VEHICLE"),
    COORDINATES("COORDINATES"),
    USER("USER"),
    IMPORT_OPERATION("IMPORT_OPERATION"),
    IMPORT_CONFLICT("IMPORT_CONFLICT")
    ;

    private final String value;

    private WsEntity(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
