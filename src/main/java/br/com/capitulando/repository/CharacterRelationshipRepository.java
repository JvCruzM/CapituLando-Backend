package br.com.capitulando.repository;

import br.com.capitulando.model.CharacterRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CharacterRelationshipRepository extends JpaRepository<CharacterRelationship, UUID> {
    List<CharacterRelationship> findAllByStory_IdOrderByCreatedAtAsc(UUID storyId);

    List<CharacterRelationship> findAllBySourceCharacter_Id(UUID characterId);

    List<CharacterRelationship> findAllByTargetCharacter_Id(UUID characterId);
}
