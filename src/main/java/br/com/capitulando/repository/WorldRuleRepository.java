package br.com.capitulando.repository;

import br.com.capitulando.model.WorldRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorldRuleRepository extends JpaRepository<WorldRule, UUID> {
    List<WorldRule> findAllByStory_IdOrderByCreatedAtAsc(UUID storyId);
}
