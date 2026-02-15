package me.zinch.is.islab2.models.dto.user;

import jakarta.validation.constraints.NotBlank;

public class UserShortDto {
    private int id;

    @NotBlank
    private String name;

    public UserShortDto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public @NotBlank String getName() {
        return name;
    }

    public void setName(@NotBlank String name) {
        this.name = name;
    }
}
