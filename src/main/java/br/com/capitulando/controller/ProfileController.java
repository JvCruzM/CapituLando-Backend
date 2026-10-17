package br.com.capitulando.controller;

import br.com.capitulando.model.Profile;
import br.com.capitulando.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<Profile> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Profile profile) {
        Profile savedProfile = profileService.save(
                authenticatedProfileId(jwt), profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProfile);
    }

    @GetMapping
    public ResponseEntity<List<Profile>> listAll(
            @AuthenticationPrincipal Jwt jwt) {
        List<Profile> profiles = profileService.findAll(
                authenticatedProfileId(jwt));
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profile> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        Profile profile = profileService.findById(
                id, authenticatedProfileId(jwt));
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<Profile> findByUsername(
            @PathVariable String username,
            @AuthenticationPrincipal Jwt jwt) {
        Profile profile = profileService.findByUsername(
                username, authenticatedProfileId(jwt));
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profile> update(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Profile profile) {
        Profile updatedProfile = profileService.update(
                id, authenticatedProfileId(jwt), profile);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        profileService.delete(id, authenticatedProfileId(jwt));
        return ResponseEntity.noContent().build();
    }

    private UUID authenticatedProfileId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
