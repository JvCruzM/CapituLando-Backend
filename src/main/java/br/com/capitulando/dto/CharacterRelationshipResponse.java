package br.com.capitulando.dto;

import java.time.Instant;
import java.util.UUID;

public record CharacterRelationshipResponse(
                UUID id,
                UUID storyId,
                UUID sourceCharacterId,
                UUID targetCharacterId,
                String type,
                String description,
                Integer level,
                Instant createdAt,
                Instant updatedAt) {
}
