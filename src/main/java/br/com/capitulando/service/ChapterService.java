package br.com.capitulando.service;

import br.com.capitulando.dto.ChapterRequest;
import br.com.capitulando.dto.ChapterResponse;
import br.com.capitulando.model.Chapter;
import br.com.capitulando.model.ChapterStatus;
import br.com.capitulando.model.Story;
import br.com.capitulando.repository.ChapterRepository;
import br.com.capitulando.repository.StoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ChapterService {

    private final ChapterRepository repository;
    private final StoryRepository storyRepository;

    public ChapterService(ChapterRepository repository, StoryRepository storyRepository) {
        this.repository = repository;
        this.storyRepository = storyRepository;
    }

    public List<ChapterResponse> findAll(UUID storyId, UUID profileId) {
        findOwnedStory(storyId, profileId);
        return repository.findByStoryIdAndStoryProfileIdOrderByOrderIndexAsc(storyId, profileId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ChapterResponse findById(UUID storyId, UUID chapterId, UUID profileId) {
        return toResponse(findOwnedChapter(storyId, chapterId, profileId));
    }

    public ChapterResponse save(UUID storyId, UUID profileId, ChapterRequest request) {
        Story story = findOwnedStory(storyId, profileId);
        Instant now = Instant.now();

        Chapter chapter = new Chapter();
        chapter.setStory(story);
        chapter.setOrderIndex(request.orderIndex() == null ? nextOrderIndex(storyId) : request.orderIndex());
        chapter.setTitle(request.title().trim());
        chapter.setContent(request.content());
        chapter.setStatus(request.status() == null ? ChapterStatus.DRAFT : request.status());
        chapter.setWordCount(countWords(request.content()));
        chapter.setCreatedAt(now);
        chapter.setUpdatedAt(now);
        if (chapter.getStatus() == ChapterStatus.PUBLISHED) {
            chapter.setPublishedAt(now);
        }

        return toResponse(repository.save(chapter));
    }

    public ChapterResponse update(UUID storyId, UUID chapterId, UUID profileId, ChapterRequest request) {
        Chapter existing = findOwnedChapter(storyId, chapterId, profileId);
        existing.setOrderIndex(request.orderIndex() == null ? existing.getOrderIndex() : request.orderIndex());
        existing.setTitle(request.title().trim());
        existing.setContent(request.content());
        existing.setWordCount(countWords(request.content()));
        if (request.status() != null) {
            existing.setStatus(request.status());
            if (request.status() == ChapterStatus.PUBLISHED && existing.getPublishedAt() == null) {
                existing.setPublishedAt(Instant.now());
            }
        }
        existing.setUpdatedAt(Instant.now());

        return toResponse(repository.save(existing));
    }

    public void delete(UUID storyId, UUID chapterId, UUID profileId) {
        repository.delete(findOwnedChapter(storyId, chapterId, profileId));
    }

    private Story findOwnedStory(UUID storyId, UUID profileId) {
        return storyRepository.findByIdAndProfileId(storyId, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "História não encontrada."));
    }

    private Chapter findOwnedChapter(UUID storyId, UUID chapterId, UUID profileId) {
        return repository.findByIdAndStoryIdAndStoryProfileId(chapterId, storyId, profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capítulo não encontrado."));
    }

    private Integer nextOrderIndex(UUID storyId) {
        return repository.findTopByStoryIdOrderByOrderIndexDesc(storyId)
                .map(chapter -> chapter.getOrderIndex() + 1)
                .orElse(1);
    }

    private Integer countWords(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        String plainText = content
                .replaceAll("(?s)<[^>]*>", " ")
                .replace("&nbsp;", " ")
                .replace("&#160;", " ");
        return plainText.trim().isEmpty() ? 0 : plainText.trim().split("\\s+").length;
    }

    private ChapterResponse toResponse(Chapter chapter) {
        return new ChapterResponse(
                chapter.getId(),
                chapter.getStory().getId(),
                chapter.getOrderIndex(),
                chapter.getTitle(),
                chapter.getContent(),
                chapter.getWordCount(),
                chapter.getStatus(),
                chapter.getPublishedAt(),
                chapter.getCreatedAt(),
                chapter.getUpdatedAt());
    }
}
