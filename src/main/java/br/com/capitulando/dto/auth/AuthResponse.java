package br.com.capitulando.dto.auth;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        AuthenticatedUserResponse user) {
}
