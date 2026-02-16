package me.zinch.is.islab3.models.fields;

import java.util.List;

public class UserField extends EnumField implements EntityField {
    public UserField(String value) {
        super(value, v -> v.equals(value), List.of("id", "name", "pubkey", "email", "isAdmin"));
    }

    @Override
    public boolean isStringType() {
        return getValue().equals("name") || getValue().equals("pubkey") || getValue().equals("email");
    }
}
