package br.com.capitulando.service;

import br.com.capitulando.model.Event;
import br.com.capitulando.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Service
public class EventService {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    public Event save(Event event) {
        Instant now = Instant.now();

        if (event.getCreatedAt() == null) {
            event.setCreatedAt(now);
        }

        event.setUpdatedAt(now);

        return repository.save(event);
    }

    public List<Event> findAll() {
        return repository.findAll();
    }

    public Event findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado para o ID: " + id));
    }

    public List<Event> findByStoryId(UUID storyId) {
        return repository.findByStoryIdOrderBySequenceOrderAsc(storyId);
    }

    public List<Event> findByChapterId(UUID chapterId) {
        return repository.findByChapterId(chapterId);
    }

    public Event update(UUID id, Event details) {
        Event existing = findById(id);

        existing.setTitle(details.getTitle());
        existing.setDescription(details.getDescription());
        existing.setChronologicalDate(details.getChronologicalDate());
        existing.setSequenceOrder(details.getSequenceOrder());
        existing.setEventImage(details.getEventImage());

        if (details.getStory() != null) {
            existing.setStory(details.getStory());
        }

        if (details.getChapter() != null) {
            existing.setChapter(details.getChapter());
        }

        if (details.getLocation() != null) {
            existing.setLocation(details.getLocation());
        }

        existing.setUpdatedAt(Instant.now());

        return repository.save(existing);
    }

    public void delete(UUID id) {
        Event existing = findById(id);
        repository.delete(existing);
    }
}
