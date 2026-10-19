package br.com.capitulando.controller;

import br.com.capitulando.dto.CharacterRelationshipRequest;
import br.com.capitulando.dto.CharacterRelationshipResponse;
import br.com.capitulando.service.CharacterRelationshipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stories/{storyId}/character-relationships")
public class CharacterRelationshipController {

    private final CharacterRelationshipService service;

    public CharacterRelationshipController(CharacterRelationshipService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CharacterRelationshipResponse>> findAll(
            @PathVariable UUID storyId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.findAll(storyId, profileId(jwt)));
    }

    @PostMapping
    public ResponseEntity<CharacterRelationshipResponse> create(
            @PathVariable UUID storyId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CharacterRelationshipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(storyId, profileId(jwt), request));
    }

    @PutMapping("/{relationshipId}")
    public ResponseEntity<CharacterRelationshipResponse> update(
            @PathVariable UUID storyId,
            @PathVariable UUID relationshipId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CharacterRelationshipRequest request) {
        return ResponseEntity.ok(service.update(storyId, relationshipId, profileId(jwt), request));
    }

    @DeleteMapping("/{relationshipId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID storyId,
            @PathVariable UUID relationshipId,
            @AuthenticationPrincipal Jwt jwt) {
        service.delete(storyId, relationshipId, profileId(jwt));
        return ResponseEntity.noContent().build();
    }

    private UUID profileId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
