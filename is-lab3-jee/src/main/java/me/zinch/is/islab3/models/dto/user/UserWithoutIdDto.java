package me.zinch.is.islab3.models.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserWithoutIdDto {
    @NotBlank
    private String name;

    @NotBlank
    private String pubkey;

    @NotBlank
    private String email;

    @NotNull
    private Boolean isAdmin;

    public UserWithoutIdDto() {
    }

    public @NotBlank String getName() {
        return name;
    }

    public void setName(@NotBlank String name) {
        this.name = name;
    }

    public @NotBlank String getPubkey() {
        return pubkey;
    }

    public void setPubkey(@NotBlank String pubkey) {
        this.pubkey = pubkey;
    }

    public @NotBlank String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank String email) {
        this.email = email;
    }

    public @NotNull Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(@NotNull Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
