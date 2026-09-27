package br.com.capitulando.repository;

import br.com.capitulando.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByStoryIdOrderBySequenceOrderAsc(UUID storyId);
    List<Event> findByChapterId(UUID chapterId);
}
