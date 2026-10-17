package br.com.capitulando.service;

import br.com.capitulando.model.Item;
import br.com.capitulando.model.Story;
import br.com.capitulando.repository.ItemRepository;
import br.com.capitulando.repository.StoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ItemService {

    private final ItemRepository repository;
    private final StoryRepository storyRepository;

    public ItemService(ItemRepository repository, StoryRepository storyRepository) {
        this.repository = repository;
        this.storyRepository = storyRepository;
    }

    public Item save(UUID profileId, Item item) {
        Story story = findOwnedStory(storyId(item), profileId);

        item.setId(null);
        item.setStory(story);

        Instant now = Instant.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);

        Item saved = repository.save(item);
        return findById(saved.getId(), profileId);
    }

    public List<Item> findAll(UUID profileId) {
        return repository.findAllByStory_Profile_Id(profileId);
    }

    public Item findById(UUID id, UUID profileId) {
        return repository.findByIdAndStory_Profile_Id(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item não encontrado."));
    }

    public List<Item> findByStoryId(UUID storyId, UUID profileId) {
        findOwnedStory(storyId, profileId);
        return repository.findByStoryIdAndStory_Profile_Id(storyId, profileId);
    }

    public Item update(UUID id, UUID profileId, Item itemDetails) {
        Item existingItem = findById(id, profileId);
        Story targetStory = existingItem.getStory();

        if (itemDetails.getStory() != null) {
            targetStory = findOwnedStory(storyId(itemDetails), profileId);
        }

        existingItem.setName(itemDetails.getName());
        existingItem.setType(itemDetails.getType());
        existingItem.setDescription(itemDetails.getDescription());
        existingItem.setAppearance(itemDetails.getAppearance());
        existingItem.setOrigin(itemDetails.getOrigin());
        existingItem.setItemImage(itemDetails.getItemImage());
        existingItem.setStory(targetStory);
        existingItem.setUpdatedAt(Instant.now());

        repository.save(existingItem);
        return findById(id, profileId);
    }

    public void delete(UUID id, UUID profileId) {
        repository.delete(findById(id, profileId));
    }

    private UUID storyId(Item item) {
        if (item.getStory() == null || item.getStory().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "História não encontrada.");
        }
        return item.getStory().getId();
    }

    private Story findOwnedStory(UUID storyId, UUID profileId) {
        return storyRepository.findByIdAndProfileId(storyId, profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "História não encontrada."));
    }
}
