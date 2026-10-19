package br.com.capitulando.controller;

import br.com.capitulando.dto.ChapterRequest;
import br.com.capitulando.dto.ChapterResponse;
import br.com.capitulando.service.ChapterService;
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
@RequestMapping("/stories/{storyId}/chapters")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @GetMapping
    public ResponseEntity<List<ChapterResponse>> listAll(
            @PathVariable UUID storyId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(chapterService.findAll(storyId, authenticatedProfileId(jwt)));
    }

    @GetMapping("/{chapterId}")
    public ResponseEntity<ChapterResponse> findById(
            @PathVariable UUID storyId,
            @PathVariable UUID chapterId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(chapterService.findById(storyId, chapterId, authenticatedProfileId(jwt)));
    }

    @PostMapping
    public ResponseEntity<ChapterResponse> create(
            @PathVariable UUID storyId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChapterRequest request) {
        ChapterResponse chapter = chapterService.save(storyId, authenticatedProfileId(jwt), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(chapter);
    }

    @PutMapping("/{chapterId}")
    public ResponseEntity<ChapterResponse> update(
            @PathVariable UUID storyId,
            @PathVariable UUID chapterId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(
                chapterService.update(storyId, chapterId, authenticatedProfileId(jwt), request));
    }

    @DeleteMapping("/{chapterId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID storyId,
            @PathVariable UUID chapterId,
            @AuthenticationPrincipal Jwt jwt) {
        chapterService.delete(storyId, chapterId, authenticatedProfileId(jwt));
        return ResponseEntity.noContent().build();
    }

    private UUID authenticatedProfileId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
