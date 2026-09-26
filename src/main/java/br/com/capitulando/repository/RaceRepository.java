package br.com.capitulando.repository;

import br.com.capitulando.model.Race;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RaceRepository extends JpaRepository<Race, UUID> {

    List<Race> findByStoryId(UUID storyId);
}