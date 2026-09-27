package br.com.capitulando.controller;

import br.com.capitulando.model.Story;
import br.com.capitulando.service.StoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // 1. CRIAR UMA HISTÓRIA (POST)
    @PostMapping
    public ResponseEntity<Story> create(@RequestBody Story story) {
        Story savedStory = storyService.save(story);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStory);
    }

    // 2. LISTAR TODAS AS HISTÓRIAS (GET)
    @GetMapping
    public ResponseEntity<List<Story>> listAll() {
        List<Story> stories = storyService.findAll();
        return ResponseEntity.ok(stories);
    }

    // 3. BUSCAR HISTÓRIA POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<Story> findById(@PathVariable UUID id) {
        Story story = storyService.findById(id);
        return ResponseEntity.ok(story);
    }

    // 4. ATUALIZAR HISTÓRIA (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Story> update(@PathVariable UUID id, @RequestBody Story story) {
        Story updatedStory = storyService.update(id, story);
        return ResponseEntity.ok(updatedStory);
    }

    // 5. DELETAR HISTÓRIA (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        storyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}