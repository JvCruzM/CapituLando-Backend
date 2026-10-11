package br.com.capitulando.repository;

import br.com.capitulando.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {
    List<Goal> findAllByProfile_IdOrderByCreatedAtDesc(UUID profileId);

    List<Goal> findAllByProfile_IdAndStory_IdOrderByCreatedAtDesc(UUID profileId, UUID storyId);
}
