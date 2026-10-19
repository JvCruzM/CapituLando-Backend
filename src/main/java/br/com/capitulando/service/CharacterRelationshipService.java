package br.com.capitulando.service;

import br.com.capitulando.dto.CharacterRelationshipRequest;
import br.com.capitulando.dto.CharacterRelationshipResponse;
import br.com.capitulando.model.Character;
import br.com.capitulando.model.CharacterRelationship;
import br.com.capitulando.model.Story;
import br.com.capitulando.repository.CharacterRelationshipRepository;
import br.com.capitulando.repository.CharacterRepository;
import br.com.capitulando.repository.StoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CharacterRelationshipService {

    private final CharacterRelationshipRepository repository;
    private final CharacterRepository characterRepository;
    private final StoryRepository storyRepository;

    public CharacterRelationshipService(
            CharacterRelationshipRepository repository,
            CharacterRepository characterRepository,
            StoryRepository storyRepository) {
        this.repository = repository;
        this.characterRepository = characterRepository;
        this.storyRepository = storyRepository;
    }

    public List<CharacterRelationshipResponse> findAll(UUID storyId, UUID profileId) {
        findOwnedStory(storyId, profileId);
        return repository.findAllByStory_IdAndStory_Profile_IdOrderByCreatedAtAsc(storyId, profileId)
                .stream().map(this::toResponse).toList();
    }

    public CharacterRelationshipResponse save(
            UUID storyId, UUID profileId, CharacterRelationshipRequest request) {
        Story story = findOwnedStory(storyId, profileId);
        Character source = findStoryCharacter(request.sourceCharacterId(), storyId);
        Character target = findStoryCharacter(request.targetCharacterId(), storyId);
        validateDifferentCharacters(source, target);

        CharacterRelationship relationship = new CharacterRelationship();
        relationship.setStory(story);
        relationship.setSourceCharacter(source);
        relationship.setTargetCharacter(target);
        applyRequest(relationship, request);
        return toResponse(repository.save(relationship));
    }

    public CharacterRelationshipResponse update(
            UUID storyId, UUID relationshipId, UUID profileId, CharacterRelationshipRequest request) {
        CharacterRelationship relationship = findOwnedRelationship(storyId, relationshipId, profileId);
        relationship.setSourceCharacter(findStoryCharacter(request.sourceCharacterId(), storyId));
        relationship.setTargetCharacter(findStoryCharacter(request.targetCharacterId(), storyId));
        validateDifferentCharacters(relationship.getSourceCharacter(), relationship.getTargetCharacter());
        applyRequest(relationship, request);
        relationship.setUpdatedAt(Instant.now());
        return toResponse(repository.save(relationship));
    }

    public void delete(UUID storyId, UUID relationshipId, UUID profileId) {
        repository.delete(findOwnedRelationship(storyId, relationshipId, profileId));
    }

    private Story findOwnedStory(UUID storyId, UUID profileId) {
        return storyRepository.findByIdAndProfileId(storyId, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "História não encontrada."));
    }

    private Character findStoryCharacter(UUID characterId, UUID storyId) {
        return characterRepository.findByIdAndStory_Id(characterId, storyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Personagem não encontrado nesta história."));
    }

    private CharacterRelationship findOwnedRelationship(UUID storyId, UUID relationshipId, UUID profileId) {
        return repository.findByIdAndStory_IdAndStory_Profile_Id(relationshipId, storyId, profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Relacionamento não encontrado."));
    }

    private void applyRequest(CharacterRelationship relationship, CharacterRelationshipRequest request) {
        relationship.setType(request.type().trim());
        relationship.setDescription(request.description());
        relationship.setLevel(request.level() == null ? 3 : Math.max(1, Math.min(5, request.level())));
    }

    private void validateDifferentCharacters(Character source, Character target) {
        if (source.getId().equals(target.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Um personagem não pode se relacionar consigo mesmo.");
        }
    }

    private CharacterRelationshipResponse toResponse(CharacterRelationship relationship) {
        return new CharacterRelationshipResponse(
                relationship.getId(), relationship.getStory().getId(),
                relationship.getSourceCharacter().getId(), relationship.getTargetCharacter().getId(),
                relationship.getType(), relationship.getDescription(), relationship.getLevel(),
                relationship.getCreatedAt(), relationship.getUpdatedAt());
    }
}
