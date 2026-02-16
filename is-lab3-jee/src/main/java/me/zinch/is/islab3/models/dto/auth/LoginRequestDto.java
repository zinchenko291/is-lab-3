package me.zinch.is.islab3.models.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDto {
    @NotBlank
    private String pubkey;

    public LoginRequestDto() {
    }

    public @NotBlank String getPubkey() {
        return pubkey;
    }

    public void setPubkey(@NotBlank String pubkey) {
        this.pubkey = pubkey;
    }
}
