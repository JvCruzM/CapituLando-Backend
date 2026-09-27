package br.com.capitulando.controller;

import br.com.capitulando.model.StoryStatus;
import br.com.capitulando.service.StoryStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/story-statuses")
public class StoryStatusController {

    private final StoryStatusService storyStatusService;

    public StoryStatusController(StoryStatusService storyStatusService) {
        this.storyStatusService = storyStatusService;
    }

   
    @GetMapping
    public ResponseEntity<List<StoryStatus>> listAll() {
        List<StoryStatus> statuses = storyStatusService.findAll();
        return ResponseEntity.ok(statuses);
    }
}