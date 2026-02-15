package me.zinch.is.islab2.models.fields;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class EnumField {
    private final String value;

    protected EnumField(String input, Predicate<? super String> predicate, List<String> allowedValues) {
        Optional<String> variant = allowedValues.stream()
                .filter(predicate)
                .findFirst();
        if (variant.isPresent()) {
            this.value = input;
        } else {
            throw new IllegalArgumentException("Invalid value: " + input);
        }
    }

    public String getValue() {
        return value;
    }
}
