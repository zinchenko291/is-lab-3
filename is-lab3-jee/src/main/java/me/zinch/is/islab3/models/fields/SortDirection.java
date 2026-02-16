package me.zinch.is.islab3.models.fields;

import java.util.List;

public class SortDirection extends EnumField {
    public SortDirection(String direction) {
        super(direction, v -> v.equalsIgnoreCase(direction), List.of("ASC", "DESC"));
    }
}