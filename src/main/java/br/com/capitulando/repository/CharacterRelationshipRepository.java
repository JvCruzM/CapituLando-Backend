package br.com.capitulando.repository;

import br.com.capitulando.model.CharacterRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CharacterRelationshipRepository extends JpaRepository<CharacterRelationship, UUID> {
    List<CharacterRelationship> findAllByStory_IdOrderByCreatedAtAsc(UUID storyId);

    List<CharacterRelationship> findAllByStory_IdAndStory_Profile_IdOrderByCreatedAtAsc(UUID storyId, UUID profileId);

    java.util.Optional<CharacterRelationship> findByIdAndStory_IdAndStory_Profile_Id(
            UUID id, UUID storyId, UUID profileId);

    List<CharacterRelationship> findAllBySourceCharacter_Id(UUID characterId);

    List<CharacterRelationship> findAllByTargetCharacter_Id(UUID characterId);

    void deleteAllByStory_IdAndSourceCharacter_Id(UUID storyId, UUID characterId);

    void deleteAllByStory_IdAndTargetCharacter_Id(UUID storyId, UUID characterId);
}
