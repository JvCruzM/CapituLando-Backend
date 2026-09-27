package br.com.capitulando.controller;

import br.com.capitulando.model.Creature;
import br.com.capitulando.service.CreatureService; // Você precisará criar este Service
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/creatures")
public class CreatureController {

    private final CreatureService CreatureService;

    // Injeção de dependência do Service via construtor
    public CreatureController(CreatureService CreatureService) {
        this.CreatureService = CreatureService;
    }

    // 1. CRIAR UMA CRIATURA (POST)
    @PostMapping
    public ResponseEntity<Creature> create(@RequestBody Creature creature) {
        Creature savedCreature = CreatureService.save(creature);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCreature); // Retorna 201 Created
    }

    // 2. LISTAR TODAS AS CRIATURAS (GET)
    @GetMapping
    public ResponseEntity<List<Creature>> listAll() {
        List<Creature> creatures = CreatureService.findAll();
        return ResponseEntity.ok(creatures); // Retorna 200 OK
    }

    // 3. BUSCAR UMA CRIATURA ESPECÍFICA POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<Creature> findById(@PathVariable UUID id) {
        Creature creature = CreatureService.findById(id);
        return ResponseEntity.ok(creature);
    }

    // 4. ATUALIZAR UMA CRIATURA (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Creature> update(@PathVariable UUID id, @RequestBody Creature creature) {
        Creature updatedCreature = CreatureService.update(id, creature);
        return ResponseEntity.ok(updatedCreature);
    }

    // 5. DELETAR UMA CRIATURA (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        CreatureService.delete(id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content (sucesso, mas sem corpo na resposta)
    }
}