package br.com.capitulando.dto;

import br.com.capitulando.model.ChapterStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChapterRequest(
                @NotBlank @Size(max = 200) String title,
                String content,
                Integer orderIndex,
                ChapterStatus status) {
}
