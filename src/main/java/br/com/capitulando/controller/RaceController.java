package br.com.capitulando.controller;

import br.com.capitulando.model.Race;
import br.com.capitulando.service.RaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // 1. CRIAR UMA RAÇA (POST)
    @PostMapping
    public ResponseEntity<Race> create(@RequestBody Race race) {
        Race savedRace = raceService.save(race);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRace);
    }

    // 2. LISTAR TODAS AS RAÇAS (GET)
    @GetMapping
    public ResponseEntity<List<Race>> listAll() {
        List<Race> races = raceService.findAll();
        return ResponseEntity.ok(races);
    }

    // 3. BUSCAR RAÇA POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<Race> findById(@PathVariable UUID id) {
        Race race = raceService.findById(id);
        return ResponseEntity.ok(race);
    }

    // 4. ATUALIZAR RAÇA (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Race> update(@PathVariable UUID id, @RequestBody Race race) {
        Race updatedRace = raceService.update(id, race);
        return ResponseEntity.ok(updatedRace);
    }

    // 5. DELETAR RAÇA (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        raceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}