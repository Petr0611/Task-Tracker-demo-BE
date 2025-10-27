package de.upteams.tasktracker.taskcomment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for updating an existing comment")
public record CommentUpdateRequestDto(
        @Schema(description = "Updated comment text", example = "Добавил уточнения по API")
        @NotBlank(message = "Comment text must not be blank")
        @Size(max = 5000, message = "Comment text is too long")
        String text
) {
}