package br.com.capitulando.repository;

import br.com.capitulando.model.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StoryRepository extends JpaRepository<Story, UUID> {

    List<Story> findByProfileId(UUID profileId);
}