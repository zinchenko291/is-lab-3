package me.zinch.is.islab3.models.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class RegisterVerifyRequestDto {
    @NotBlank
    private String pubkey;

    @NotBlank
    private String signature;

    public RegisterVerifyRequestDto() {
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
