package br.com.capitulando.controller;

import br.com.capitulando.model.Creature;
import br.com.capitulando.service.CreatureService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/creatures")
public class CreatureController {

    private final CreatureService creatureService;

    public CreatureController(CreatureService creatureService) {
        this.creatureService = creatureService;
    }

    @PostMapping
    public ResponseEntity<Creature> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Creature creature) {
        Creature savedCreature = creatureService.save(
                authenticatedProfileId(jwt),
                creature);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedCreature);
    }

    @GetMapping
    public ResponseEntity<List<Creature>> listAll(
            @AuthenticationPrincipal Jwt jwt) {
        List<Creature> creatures = creatureService.findAll(
                authenticatedProfileId(jwt));

        return ResponseEntity.ok(creatures);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Creature> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        Creature creature = creatureService.findById(
                id,
                authenticatedProfileId(jwt));

        return ResponseEntity.ok(creature);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Creature> update(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Creature creature) {
        Creature updatedCreature = creatureService.update(
                id,
                authenticatedProfileId(jwt),
                creature);

        return ResponseEntity.ok(updatedCreature);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        creatureService.delete(
                id,
                authenticatedProfileId(jwt));

        return ResponseEntity.noContent().build();
    }

    private UUID authenticatedProfileId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}