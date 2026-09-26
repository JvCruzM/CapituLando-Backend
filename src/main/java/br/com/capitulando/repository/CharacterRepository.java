package br.com.capitulando.repository;

import br.com.capitulando.model.Character;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CharacterRepository extends JpaRepository<Character, UUID> {

    List<Character> findByStoryId(UUID storyId);
}