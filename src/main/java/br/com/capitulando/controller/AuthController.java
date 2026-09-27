package br.com.capitulando.controller;

import br.com.capitulando.model.Profile;
import br.com.capitulando.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controladora para gerenciamento de autenticação e rotas de login/registro.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ProfileService profileService;

    public AuthController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // DTOs de autenticação
    public record LoginRequest(String username, String password) {}
    public record RegisterRequest(String username, String password, String displayName, String bio, String avatarImage) {}
    public record AuthResponse(UUID id, String username, String displayName, String token) {}

    // 1. LOGIN DE EXEMPLO (POST /auth/login)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            Profile profile = profileService.findByUsername(request.username());
            AuthResponse response = new AuthResponse(
                    profile.getId(),
                    profile.getUsername(),
                    profile.getDisplayName(),
                    "sample-token"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 2. CADASTRO DE EXEMPLO (POST /auth/register)
    @PostMapping("/register")
    public ResponseEntity<Profile> register(@RequestBody RegisterRequest request) {
        Profile newProfile = new Profile();
        newProfile.setId(UUID.randomUUID());
        newProfile.setUsername(request.username());
        newProfile.setDisplayName(request.displayName());
        newProfile.setBio(request.bio());
        newProfile.setAvatarImage(request.avatarImage());

        Profile savedProfile = profileService.save(newProfile);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProfile);
    }
}
