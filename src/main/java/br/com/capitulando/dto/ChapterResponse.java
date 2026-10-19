package br.com.capitulando.dto;

import br.com.capitulando.model.ChapterStatus;

import java.time.Instant;
import java.util.UUID;

public record ChapterResponse(
                UUID id,
                UUID storyId,
                Integer orderIndex,
                String title,
                String content,
                Integer wordCount,
                ChapterStatus status,
                Instant publishedAt,
                Instant createdAt,
                Instant updatedAt) {
}
