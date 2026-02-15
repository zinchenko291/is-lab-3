package me.zinch.is.islab2.models.entities;

public enum VehicleType {
    BOAT("BOAT"),
    SHIP("SHIP"),
    MOTORCYCLE("MOTORCYCLE"),
    CHOPPER("CHOPPER")
    ;

    private final String value;

    VehicleType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
