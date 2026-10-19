package br.com.capitulando.service;

import br.com.capitulando.model.Character;
import br.com.capitulando.model.Race;
import br.com.capitulando.model.Story;
import br.com.capitulando.repository.CharacterRepository;
import br.com.capitulando.repository.CharacterRelationshipRepository;
import br.com.capitulando.repository.RaceRepository;
import br.com.capitulando.repository.StoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CharacterService {

        private final CharacterRepository repository;
        private final StoryRepository storyRepository;
        private final RaceRepository raceRepository;
        private final CharacterRelationshipRepository relationshipRepository;

        public CharacterService(
                        CharacterRepository repository,
                        StoryRepository storyRepository,
                        RaceRepository raceRepository,
                        CharacterRelationshipRepository relationshipRepository) {
                this.repository = repository;
                this.storyRepository = storyRepository;
                this.raceRepository = raceRepository;
                this.relationshipRepository = relationshipRepository;
        }

        @Transactional
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

                character.setRace(resolveRace(character.getRace(), story.getId(), profileId));

                character.setId(null);
                character.setStory(story);

                Instant now = Instant.now();

                character.setCreatedAt(now);
                character.setUpdatedAt(now);

                return repository.save(character);
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

        @Transactional
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

                Story storyForRace = targetStory != null ? targetStory : existingCharacter.getStory();
                if (characterDetails.getRace() != null) {
                        existingCharacter.setRace(resolveRace(
                                        characterDetails.getRace(), storyForRace.getId(), profileId));
                }

                existingCharacter.setName(characterDetails.getName());
                existingCharacter.setDescription(characterDetails.getDescription());
                existingCharacter.setAppearance(characterDetails.getAppearance());
                existingCharacter.setPersonality(characterDetails.getPersonality());
                existingCharacter.setAge(characterDetails.getAge());
                existingCharacter.setHeight(characterDetails.getHeight());
                existingCharacter.setWeight(characterDetails.getWeight());
                existingCharacter.setZodiac(characterDetails.getZodiac());
                existingCharacter.setBloodType(characterDetails.getBloodType());
                existingCharacter.setGender(characterDetails.getGender());
                existingCharacter.setBackground(characterDetails.getBackground());
                existingCharacter.setCharacterImage(characterDetails.getCharacterImage());
                existingCharacter.setImageFit(characterDetails.getImageFit());
                existingCharacter.setImagePosition(characterDetails.getImagePosition());
                existingCharacter.setLikes(characterDetails.getLikes());
                existingCharacter.setDislikes(characterDetails.getDislikes());
                existingCharacter.setDrinks(characterDetails.getDrinks());
                existingCharacter.setMotto(characterDetails.getMotto());
                existingCharacter.setRole(characterDetails.getRole());
                existingCharacter.setMbti(characterDetails.getMbti());
                existingCharacter.setMotivation(characterDetails.getMotivation());
                existingCharacter.setNotes(characterDetails.getNotes());
                existingCharacter.setBirthDate(characterDetails.getBirthDate());

                if (targetStory != null) {
                        existingCharacter.setStory(targetStory);
                }

                existingCharacter.setUpdatedAt(Instant.now());

                return repository.save(existingCharacter);
        }

        @Transactional
        public void delete(UUID id, UUID profileId) {
                Character existingCharacter = findById(id, profileId);
                relationshipRepository.deleteAllByStory_IdAndSourceCharacter_Id(
                                existingCharacter.getStory().getId(), id);
                relationshipRepository.deleteAllByStory_IdAndTargetCharacter_Id(
                                existingCharacter.getStory().getId(), id);
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

        private Race resolveRace(Race requestedRace, UUID storyId, UUID profileId) {
                if (requestedRace == null) {
                        return null;
                }
                if (requestedRace.getId() == null) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Raça não encontrada.");
                }
                return raceRepository.findByIdAndStory_IdAndStory_Profile_Id(
                                requestedRace.getId(), storyId, profileId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Raça não encontrada nesta história."));
        }
}
