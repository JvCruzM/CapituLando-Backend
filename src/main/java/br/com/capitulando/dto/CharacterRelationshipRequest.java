package br.com.capitulando.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CharacterRelationshipRequest(
                @NotNull UUID sourceCharacterId,
                @NotNull UUID targetCharacterId,
                @NotBlank @Size(max = 80) String type,
                @Size(max = 5000) String description,
                Integer level) {
}
