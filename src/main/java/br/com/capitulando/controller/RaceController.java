package br.com.capitulando.controller;

import br.com.capitulando.model.Race;
import br.com.capitulando.service.RaceService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/races")
public class RaceController {

    private final RaceService raceService;

    public RaceController(RaceService raceService) {
        this.raceService = raceService;
    }

    @PostMapping
    public ResponseEntity<Race> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Race race) {
        Race savedRace = raceService.save(
                authenticatedProfileId(jwt),
                race);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedRace);
    }

    @GetMapping
    public ResponseEntity<List<Race>> listAll(
            @AuthenticationPrincipal Jwt jwt) {
        List<Race> races = raceService.findAll(
                authenticatedProfileId(jwt));

        return ResponseEntity.ok(races);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Race> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        Race race = raceService.findById(
                id,
                authenticatedProfileId(jwt));

        return ResponseEntity.ok(race);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Race> update(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Race race) {
        Race updatedRace = raceService.update(
                id,
                authenticatedProfileId(jwt),
                race);

        return ResponseEntity.ok(updatedRace);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        raceService.delete(
                id,
                authenticatedProfileId(jwt));

        return ResponseEntity.noContent().build();
    }

    private UUID authenticatedProfileId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}