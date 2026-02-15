package me.zinch.is.islab2.models.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class LoginVerifyRequestDto {
    @NotBlank
    private String pubkey;

    @NotBlank
    private String signature;

    public LoginVerifyRequestDto() {
    }

    public @NotBlank String getPubkey() {
        return pubkey;
    }

    public void setPubkey(@NotBlank String pubkey) {
        this.pubkey = pubkey;
    }

    public @NotBlank String getSignature() {
        return signature;
    }

    public void setSignature(@NotBlank String signature) {
        this.signature = signature;
    }
}
