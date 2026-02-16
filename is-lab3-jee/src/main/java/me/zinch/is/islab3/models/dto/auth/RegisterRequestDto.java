package me.zinch.is.islab3.models.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class RegisterRequestDto {
    @NotBlank
    private String name;

    @NotBlank
    private String pubkey;

    @NotBlank
    private String email;

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
}
