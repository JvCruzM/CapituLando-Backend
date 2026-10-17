package br.com.capitulando.controller;

import br.com.capitulando.model.Story;
import br.com.capitulando.service.StoryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stories")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @PostMapping
    public ResponseEntity<Story> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Story story) {

        Story savedStory = storyService.save(
                authenticatedProfileId(jwt),
                story);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedStory);
    }

    @GetMapping
    public ResponseEntity<List<Story>> listAll(
            @AuthenticationPrincipal Jwt jwt) {

        List<Story> stories = storyService.findAll(
                authenticatedProfileId(jwt));

        return ResponseEntity.ok(stories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Story> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        Story story = storyService.findById(
                id,
                authenticatedProfileId(jwt));

        return ResponseEntity.ok(story);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Story> update(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Story story) {

        Story updatedStory = storyService.update(
                id,
                authenticatedProfileId(jwt),
                story);

        return ResponseEntity.ok(updatedStory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        storyService.delete(
                id,
                authenticatedProfileId(jwt));

        return ResponseEntity.noContent().build();
    }

    private UUID authenticatedProfileId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}