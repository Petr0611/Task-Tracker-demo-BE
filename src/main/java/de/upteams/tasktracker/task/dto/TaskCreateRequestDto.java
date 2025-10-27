package de.upteams.tasktracker.task.dto;

import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import de.upteams.tasktracker.task.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for creating a Task
 *
 * @param title       Title of the new task
 * @param description Description of the task
 * @param status      Initial status of the task
 * @param columnId    Identifier of the column the task should belong to
 */
@Schema(description = "Request DTO for creating a Task")
public record TaskCreateRequestDto(
        @Schema(
                description = "Title of the new Task",
                example = "Implement repository layer"
        )
        @NotBlank
        String title,

        @Schema(
                description = "Detailed description of the Task",
                example = "Create JPA repositories for all entities"
        )
        @NotBlank
        String description,

        @Schema(
                description = "Initial status of the Task",
                example = "NEW"
        )
        TaskStatus status,

        @Schema(
                description = "Identifier of the column the Task should be placed into",
                example = "5b70c020-07a5-43c5-b4db-5bd4f72bae94"
        )
        @NotBlank
        @Pattern(regexp = TaskValidationConstats.UUID_PATTERN, message = TaskValidationConstats.COLUMN_ID_INVALID_MESSAGE)
        String columnId
) {
}