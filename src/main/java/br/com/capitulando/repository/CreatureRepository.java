package br.com.capitulando.repository;

import br.com.capitulando.model.Creature;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreatureRepository
        extends JpaRepository<Creature, UUID> {

    List<Creature> findByStoryId(UUID storyId);

    List<Creature> findByStoryIdAndStory_Profile_Id(
            UUID storyId,
            UUID profileId);

    @EntityGraph(attributePaths = {
            "story.profile",
            "race.story.profile"
    })
    List<Creature> findAllByStory_Profile_Id(UUID profileId);

    @EntityGraph(attributePaths = {
            "story.profile",
            "race.story.profile"
    })
    Optional<Creature> findByIdAndStory_Profile_Id(
            UUID id,
            UUID profileId);
}