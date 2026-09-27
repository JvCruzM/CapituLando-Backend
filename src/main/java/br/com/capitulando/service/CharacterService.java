package br.com.capitulando.service;

import br.com.capitulando.model.Character;
import br.com.capitulando.repository.CharacterRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CharacterService {

    private final CharacterRepository repository;

    public CharacterService(CharacterRepository repository) {
        this.repository = repository;
    }

    public Character save(Character character) {
        Instant now = Instant.now();

        if (character.getCreatedAt() == null) {
            character.setCreatedAt(now);
        }

        character.setUpdatedAt(now);

        return repository.save(character);
    }

    public List<Character> findAll() {
        return repository.findAll();
    }

    public Character findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Personagem não encontrado para o ID: " + id));
    }

    public List<Character> findByStoryId(UUID storyId) {
        return repository.findByStoryId(storyId);
    }

    public Character update(UUID id, Character characterDetails) {
        Character existingCharacter = findById(id);

        existingCharacter.setName(characterDetails.getName());
        existingCharacter.setDescription(characterDetails.getDescription());
        existingCharacter.setAppearance(characterDetails.getAppearance());
        existingCharacter.setPersonality(characterDetails.getPersonality());
        existingCharacter.setAge(characterDetails.getAge());
        existingCharacter.setGender(characterDetails.getGender());
        existingCharacter.setBackground(characterDetails.getBackground());
        existingCharacter.setCharacterImage(characterDetails.getCharacterImage());

        if (characterDetails.getStory() != null) {
            existingCharacter.setStory(characterDetails.getStory());
        }

        if (characterDetails.getRace() != null) {
            existingCharacter.setRace(characterDetails.getRace());
        }

        existingCharacter.setUpdatedAt(Instant.now());

        return repository.save(existingCharacter);
    }

    public void delete(UUID id) {
        Character existingCharacter = findById(id);
        repository.delete(existingCharacter);
    }
}