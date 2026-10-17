package br.com.capitulando.controller;

import br.com.capitulando.model.Item;
import br.com.capitulando.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<Item> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Item item) {
        Item savedItem = itemService.save(authenticatedProfileId(jwt), item);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @GetMapping
    public ResponseEntity<List<Item>> listAll(@AuthenticationPrincipal Jwt jwt) {
        List<Item> items = itemService.findAll(authenticatedProfileId(jwt));
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        Item item = itemService.findById(id, authenticatedProfileId(jwt));
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> update(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Item item) {
        Item updatedItem = itemService.update(
                id, authenticatedProfileId(jwt), item);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        itemService.delete(id, authenticatedProfileId(jwt));
        return ResponseEntity.noContent().build();
    }

    private UUID authenticatedProfileId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
