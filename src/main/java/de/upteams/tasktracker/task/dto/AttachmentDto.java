package de.upteams.tasktracker.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO that represents a single attachment linked to a task.
 *
 * @param id  Unique identifier of the attachment
 * @param url Public URL of the stored attachment
 */
@Schema(description = "Data Transfer Object for task attachment")
public record AttachmentDto(
        @Schema(description = "Unique identifier of the attachment", example = "6f0f0d5a-2b03-4f9d-8e12-9e06f4d7c8f1")
        String id,

        @Schema(description = "Public URL of the attachment", example = "https://cdn.example.com/attachments/task123_doc.png")
        String url
) {
}