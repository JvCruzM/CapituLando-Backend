package br.com.capitulando.repository;

import br.com.capitulando.model.Idea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IdeaRepository extends JpaRepository<Idea, UUID> {
    List<Idea> findAllByProfile_IdOrderByCreatedAtDesc(UUID profileId);

    List<Idea> findAllByProfile_IdAndStory_IdOrderByCreatedAtDesc(UUID profileId, UUID storyId);
}
