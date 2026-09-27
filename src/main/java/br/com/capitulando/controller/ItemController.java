package br.com.capitulando.controller;

import br.com.capitulando.model.Item;
import br.com.capitulando.service.ItemService; // Você precisará criar este Service
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    // Injeção de dependência do Service via construtor
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // 1. CRIAR UM ITEM (POST)
    @PostMapping
    public ResponseEntity<Item> create(@RequestBody Item item) {
        Item savedItem = itemService.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem); // Retorna 201 Created
    }

    // 2. LISTAR TODOS OS ITENS (GET)
    @GetMapping
    public ResponseEntity<List<Item>> listAll() {
        List<Item> items = itemService.findAll();
        return ResponseEntity.ok(items); // Retorna 200 OK
    }

    // 3. BUSCAR UM ITEM ESPECÍFICO POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<Item> findById(@PathVariable UUID id) {
        Item item = itemService.findById(id);
        return ResponseEntity.ok(item); // Retorna 200 OK
    }

    // 4. ATUALIZAR UM ITEM (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Item> update(@PathVariable UUID id, @RequestBody Item item) {
        Item updatedItem = itemService.update(id, item);
        return ResponseEntity.ok(updatedItem); // Retorna 200 OK
    }

    // 5. DELETAR UM ITEM (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}