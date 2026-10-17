package br.com.capitulando.service;

import br.com.capitulando.model.Character;
import br.com.capitulando.model.Story;
import br.com.capitulando.repository.CharacterRepository;
import br.com.capitulando.repository.StoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CharacterService {

        private final CharacterRepository repository;
        private final StoryRepository storyRepository;

        public CharacterService(
                        CharacterRepository repository,
                        StoryRepository storyRepository) {
                this.repository = repository;
                this.storyRepository = storyRepository;
        }

        public Character save(UUID profileId, Character character) {
                if (character.getStory() == null
                                || character.getStory().getId() == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "História não encontrada.");
                }

                Story story = findOwnedStory(
                                character.getStory().getId(),
                                profileId);

                character.setId(null);
                character.setStory(story);

                Instant now = Instant.now();

                character.setCreatedAt(now);
                character.setUpdatedAt(now);

                Character saved = repository.save(character);

                return findById(saved.getId(), profileId);
        }

        public List<Character> findAll(UUID profileId) {
                return repository.findAllByStory_Profile_Id(profileId);
        }

        public Character findById(UUID id, UUID profileId) {
                return repository.findByIdAndStory_Profile_Id(
                                id,
                                profileId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Personagem não encontrado."));
        }

        public Character update(
                        UUID id,
                        UUID profileId,
                        Character characterDetails) {
                Character existingCharacter = findById(id, profileId);

                Story targetStory = null;

                if (characterDetails.getStory() != null) {
                        UUID storyId = characterDetails.getStory().getId();

                        if (storyId == null) {
                                throw new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "História não encontrada.");
                        }

                        targetStory = findOwnedStory(storyId, profileId);
                }

                existingCharacter.setName(characterDetails.getName());
                existingCharacter.setDescription(characterDetails.getDescription());
                existingCharacter.setAppearance(characterDetails.getAppearance());
                existingCharacter.setPersonality(characterDetails.getPersonality());
                existingCharacter.setAge(characterDetails.getAge());
                existingCharacter.setGender(characterDetails.getGender());
                existingCharacter.setBackground(characterDetails.getBackground());
                existingCharacter.setCharacterImage(characterDetails.getCharacterImage());
                existingCharacter.setRole(characterDetails.getRole());
                existingCharacter.setMbti(characterDetails.getMbti());
                existingCharacter.setMotivation(characterDetails.getMotivation());
                existingCharacter.setNotes(characterDetails.getNotes());
                existingCharacter.setBirthDate(characterDetails.getBirthDate());

                if (targetStory != null) {
                        existingCharacter.setStory(targetStory);
                }

                if (characterDetails.getRace() != null) {
                        existingCharacter.setRace(characterDetails.getRace());
                }

                existingCharacter.setUpdatedAt(Instant.now());

                repository.save(existingCharacter);

                return findById(id, profileId);
        }

        public void delete(UUID id, UUID profileId) {
                Character existingCharacter = findById(id, profileId);
                repository.delete(existingCharacter);
        }

        private Story findOwnedStory(UUID storyId, UUID profileId) {
                return storyRepository.findByIdAndProfileId(
                                storyId,
                                profileId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "História não encontrada."));
        }
}