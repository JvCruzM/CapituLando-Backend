package br.com.capitulando.repository;

import br.com.capitulando.model.Creature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CreatureRepository extends JpaRepository<Creature, UUID> {

    List<Creature> findByStoryId(UUID storyId);
}