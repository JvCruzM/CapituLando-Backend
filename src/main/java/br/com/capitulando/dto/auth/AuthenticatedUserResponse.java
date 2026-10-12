package br.com.capitulando.dto.auth;

import java.util.UUID;

public record AuthenticatedUserResponse(
        UUID id,
        String email,
        boolean emailVerified,
        String username,
        String displayName) {
}
