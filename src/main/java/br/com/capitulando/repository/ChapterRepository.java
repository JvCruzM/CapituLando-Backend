package br.com.capitulando.repository;

import br.com.capitulando.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, UUID> {
    List<Chapter> findByStoryIdAndStoryProfileIdOrderByOrderIndexAsc(UUID storyId, UUID profileId);

    Optional<Chapter> findByIdAndStoryIdAndStoryProfileId(UUID id, UUID storyId, UUID profileId);

    Optional<Chapter> findTopByStoryIdOrderByOrderIndexDesc(UUID storyId);
}
