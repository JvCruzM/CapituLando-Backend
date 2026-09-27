package br.com.capitulando.service;

import br.com.capitulando.model.Item;
import br.com.capitulando.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    // SALVAR
    public Item save(Item item) {
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
        return repository.save(item);
    }

    // LISTAR TODOS
    public List<Item> findAll() {
        return repository.findAll();
    }

    // BUSCAR POR ID
    public Item findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado para o ID: " + id));
    }

    // ATUALIZAR
    public Item update(UUID id, Item itemDetails) {
        Item existingItem = findById(id);

        // Atualiza os campos de texto
        existingItem.setName(itemDetails.getName());
        existingItem.setType(itemDetails.getType());
        existingItem.setDescription(itemDetails.getDescription());
        existingItem.setAppearance(itemDetails.getAppearance());
        existingItem.setOrigin(itemDetails.getOrigin());
        existingItem.setItemImage(itemDetails.getItemImage());
        
        // Só atualiza a história se ela for enviada na requisição
        if (itemDetails.getStory() != null) {
            existingItem.setStory(itemDetails.getStory());
        }

        // Atualiza a data de modificação
        existingItem.setUpdatedAt(Instant.now());

        return repository.save(existingItem);
    }

    // DELETAR
    public void delete(UUID id) {
        Item existingItem = findById(id);
        repository.delete(existingItem);
    }
}