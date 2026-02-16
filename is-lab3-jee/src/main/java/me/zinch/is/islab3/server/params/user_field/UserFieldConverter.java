package me.zinch.is.islab3.server.params.user_field;

import jakarta.ws.rs.ext.ParamConverter;
import me.zinch.is.islab3.exceptions.DeserializingException;
import me.zinch.is.islab3.models.fields.UserField;

public class UserFieldConverter implements ParamConverter<UserField> {
    @Override
    public UserField fromString(String s) {
        try {
            return new UserField(s);
        } catch (IllegalArgumentException e) {
            throw new DeserializingException(
                    String.format("Invalid field %s for User", s)
            );
        }
    }

    @Override
    public String toString(UserField userField) {
        return userField.getValue();
    }
}
