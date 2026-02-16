package me.zinch.is.islab3.models.fields;

import java.util.List;

public class VehicleField extends EnumField implements EntityField {
    public VehicleField(String value) {
        super(value, v -> v.equals(value), List.of("id", "name", "x", "y", "creationDate", "type", "enginePower",
                "numberOfWheels", "capacity", "distanceTravelled", "fuelConsumption", "fuelType"));
    }

    @Override
    public boolean isStringType() {
        return getValue().equals("name");
    }
}