package de.upteams.tasktracker.task.dto;

import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for updating a Task
 *
 * @param title       New title of the Task
 * @param description New description of the Task
 * @param columnId    Identifier of the column the Task should be moved to
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
    String description,

    @Schema(
            description = "Identifier of the column the Task should be moved to",
            example = "5b70c020-07a5-43c5-b4db-5bd4f72bae94"
    )
    @Pattern(regexp = TaskValidationConstats.UUID_PATTERN, message = TaskValidationConstats.COLUMN_ID_INVALID_MESSAGE)
    String columnId
) {
    }

