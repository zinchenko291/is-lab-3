package me.zinch.is.islab3.models.dto.auth;

public class ChallengeResponseDto {
    private String challenge;

    public ChallengeResponseDto() {
    }

    public ChallengeResponseDto(String challenge) {
        this.challenge = challenge;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }
}
