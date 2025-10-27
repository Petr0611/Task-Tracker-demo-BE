package de.upteams.tasktracker.taskcomment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.time.LocalDateTime;

@Schema(description = "Data Transfer Object for Task Comment entity")
@Value
public class CommentDto {

    @Schema(description = "Unique identifier of the Comment", accessMode = Schema.AccessMode.READ_ONLY)
    String id;

    @Schema(description = "Identifier of the Task the comment belongs to")
    String taskId;

    @Schema(description = "Identifier of the comment author", accessMode = Schema.AccessMode.READ_ONLY)
    String authorId;

    @Schema(description = "Display name of the comment author", accessMode = Schema.AccessMode.READ_ONLY)
    String authorDisplayName;

    @Schema(description = "Email of the comment author", accessMode = Schema.AccessMode.READ_ONLY)
    String authorEmail;

    @Schema(description = "Text of the comment")
    String text;

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
    LocalDateTime createdAt;
}