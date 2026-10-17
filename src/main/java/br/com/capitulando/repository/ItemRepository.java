package br.com.capitulando.repository;

import br.com.capitulando.model.Item;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    List<Item> findByStoryIdAndStory_Profile_Id(UUID storyId, UUID profileId);

    @EntityGraph(attributePaths = "story.profile")
    List<Item> findAllByStory_Profile_Id(UUID profileId);

    @EntityGraph(attributePaths = "story.profile")
    Optional<Item> findByIdAndStory_Profile_Id(UUID id, UUID profileId);
}
