package br.com.capitulando.controller;

import br.com.capitulando.model.Character;
import br.com.capitulando.service.CharacterService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/characters")
public class CharacterController {

        private final CharacterService characterService;

        public CharacterController(CharacterService characterService) {
                this.characterService = characterService;
        }

        @PostMapping
        public ResponseEntity<Character> create(
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestBody Character character) {
                Character savedCharacter = characterService.save(
                                authenticatedProfileId(jwt),
                                character);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(savedCharacter);
        }

        @GetMapping
        public ResponseEntity<List<Character>> listAll(
                        @AuthenticationPrincipal Jwt jwt) {
                List<Character> characters = characterService.findAll(
                                authenticatedProfileId(jwt));

                return ResponseEntity.ok(characters);
        }

        @GetMapping("/{id}")
        public ResponseEntity<Character> findById(
                        @PathVariable UUID id,
                        @AuthenticationPrincipal Jwt jwt) {
                Character character = characterService.findById(
                                id,
                                authenticatedProfileId(jwt));

                return ResponseEntity.ok(character);
        }

        @PutMapping("/{id}")
        public ResponseEntity<Character> update(
                        @PathVariable UUID id,
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestBody Character character) {
                Character updatedCharacter = characterService.update(
                                id,
                                authenticatedProfileId(jwt),
                                character);

                return ResponseEntity.ok(updatedCharacter);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
                        @PathVariable UUID id,
                        @AuthenticationPrincipal Jwt jwt) {
                characterService.delete(
                                id,
                                authenticatedProfileId(jwt));

                return ResponseEntity.noContent().build();
        }

        private UUID authenticatedProfileId(Jwt jwt) {
                return UUID.fromString(jwt.getSubject());
        }
}