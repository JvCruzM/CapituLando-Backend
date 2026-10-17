package br.com.capitulando.repository;

import br.com.capitulando.model.Story;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoryRepository extends JpaRepository<Story, UUID> {

    @EntityGraph(attributePaths = "profile")
    List<Story> findAllByProfileId(UUID profileId);

    @EntityGraph(attributePaths = "profile")
    Optional<Story> findByIdAndProfileId(UUID id, UUID profileId);
}