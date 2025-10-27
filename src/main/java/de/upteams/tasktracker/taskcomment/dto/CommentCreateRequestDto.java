package de.upteams.tasktracker.taskcomment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating a new comment")
public record CommentCreateRequestDto(
        @Schema(description = "Comment text", example = "Пожалуйста, уточните требования")
        @NotBlank(message = "Comment text must not be blank")
        @Size(max = 5000, message = "Comment text is too long")
        String text
) {
}