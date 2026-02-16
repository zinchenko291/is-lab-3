package me.zinch.is.islab3.models.fields;

import java.util.List;

public class CoordinatesField extends EnumField implements EntityField  {
    public CoordinatesField(String value) {
        super(value, v -> v.equals(value), List.of("id", "x", "y"));
    }

    @Override
    public boolean isStringType() {
        return false;
    }
}