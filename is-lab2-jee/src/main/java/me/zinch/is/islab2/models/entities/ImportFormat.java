package me.zinch.is.islab2.models.entities;

public enum ImportFormat {
    YAML("YAML"),
    XML("XML")
    ;

    private final String value;

    ImportFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
