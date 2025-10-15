package de.upteams.tasktracker.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for updating a Task
 *
 * @param title       New title of the Task
 * @param description New description of the Task
 */
@Schema(description = "Request DTO for updating a Task")
public record TaskUpdateRequestDto(
        @Schema(
                description = "New title of the Task",
                example = "Fix login bug"
        )
        String title,

        @Schema(
                description = "New description of the Task",
                example = "Resolve issue with login form validation"
        )
        String description) {

}

