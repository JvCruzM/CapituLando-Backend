package br.com.capitulando.service;

import br.com.capitulando.model.Chapter;
import br.com.capitulando.repository.ChapterRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ChapterService {

    private final ChapterRepository repository;

    public ChapterService(ChapterRepository repository) {
        this.repository = repository;
    }

    public Chapter save(Chapter chapter) {
        Instant now = Instant.now();

        if (chapter.getCreatedAt() == null) {
            chapter.setCreatedAt(now);
        }

        chapter.setUpdatedAt(now);

        chapter.setWordCount(countWords(chapter.getContent()));

        return repository.save(chapter);
    }

    public List<Chapter> findAll() {
        return repository.findAll();
    }

    public Chapter findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Capítulo não encontrado para o ID: " + id));
    }

    public List<Chapter> findByStoryId(UUID storyId) {
        return repository.findByStoryIdOrderByOrderIndexAsc(storyId);
    }

    public Chapter update(UUID id, Chapter details) {
        Chapter existing = findById(id);

        existing.setOrderIndex(details.getOrderIndex());
        existing.setTitle(details.getTitle());
        existing.setContent(details.getContent());

        if (details.getStatus() != null) {
            existing.setStatus(details.getStatus());
        }

        if (details.getPublishedAt() != null) {
            existing.setPublishedAt(details.getPublishedAt());
        }

        if (details.getContent() != null) {
            existing.setWordCount(countWords(details.getContent()));
        }

        if (details.getStory() != null) {
            existing.setStory(details.getStory());
        }

        existing.setUpdatedAt(Instant.now());

        return repository.save(existing);
    }

    public void delete(UUID id) {
        Chapter existing = findById(id);
        repository.delete(existing);
    }

    private Integer countWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        return content.trim().split("\\s+").length;
    }
}
