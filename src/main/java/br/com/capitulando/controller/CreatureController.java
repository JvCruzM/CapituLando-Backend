package br.com.capitulando.controller;

import br.com.capitulando.model.Creature;
import br.com.capitulando.service.CreatureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // 1. CRIAR UMA CRIATURA (POST)
    @PostMapping
    public ResponseEntity<Creature> create(@RequestBody Creature creature) {
        Creature savedCreature = creatureService.save(creature);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCreature);
    }

    // 2. LISTAR TODAS AS CRIATURAS (GET)
    @GetMapping
    public ResponseEntity<List<Creature>> listAll() {
        List<Creature> creatures = creatureService.findAll();
        return ResponseEntity.ok(creatures);
    }

    // 3. BUSCAR UMA CRIATURA POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<Creature> findById(@PathVariable UUID id) {
        Creature creature = creatureService.findById(id);
        return ResponseEntity.ok(creature);
    }

    // 4. ATUALIZAR UMA CRIATURA (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Creature> update(
            @PathVariable UUID id,
            @RequestBody Creature creature
    ) {
        Creature updatedCreature = creatureService.update(id, creature);
        return ResponseEntity.ok(updatedCreature);
    }

    // 5. DELETAR UMA CRIATURA (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        creatureService.delete(id);
        return ResponseEntity.noContent().build();
    }
}