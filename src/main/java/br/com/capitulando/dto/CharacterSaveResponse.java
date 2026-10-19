package br.com.capitulando.dto;

import java.util.UUID;

public record CharacterSaveResponse(UUID id, UUID storyId) {
}
