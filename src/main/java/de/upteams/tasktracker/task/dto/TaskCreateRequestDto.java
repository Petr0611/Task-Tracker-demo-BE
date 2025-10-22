package de.upteams.tasktracker.task.dto;

import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for creating a Task
 *
 * @param title       Title of the new task
 * @param description Description of the task
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
        String description
) {
}