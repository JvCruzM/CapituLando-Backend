package br.com.capitulando.repository;

import br.com.capitulando.model.Character;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CharacterRepository
                extends JpaRepository<Character, UUID> {

        List<Character> findByStoryId(UUID storyId);

        Optional<Character> findByIdAndStory_Id(UUID id, UUID storyId);

        long countByIdInAndStory_IdAndStory_Profile_Id(
                        List<UUID> ids, UUID storyId, UUID profileId);

        @EntityGraph(attributePaths = {
                        "story.profile",
                        "race.story.profile"
        })
        List<Character> findAllByStory_Profile_Id(UUID profileId);

        @EntityGraph(attributePaths = {
                        "story.profile",
                        "race.story.profile"
        })
        Optional<Character> findByIdAndStory_Profile_Id(
                        UUID id,
                        UUID profileId);
}
