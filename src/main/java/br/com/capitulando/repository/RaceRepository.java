package br.com.capitulando.repository;

import br.com.capitulando.model.Race;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RaceRepository extends JpaRepository<Race, UUID> {

    List<Race> findByStoryId(UUID storyId);

    List<Race> findByStoryIdAndStory_Profile_Id(
            UUID storyId,
            UUID profileId);

    @EntityGraph(attributePaths = "story.profile")
    List<Race> findAllByStory_Profile_Id(UUID profileId);

    @EntityGraph(attributePaths = "story.profile")
    Optional<Race> findByIdAndStory_Profile_Id(
            UUID id,
            UUID profileId);
}