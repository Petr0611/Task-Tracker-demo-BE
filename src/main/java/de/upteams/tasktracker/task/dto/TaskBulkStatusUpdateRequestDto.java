package de.upteams.tasktracker.task.dto;

import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import de.upteams.tasktracker.task.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request DTO for updating the status of multiple tasks")
public record TaskBulkStatusUpdateRequestDto(
        @Schema(description = "Identifiers of tasks whose status should be changed", required = true)
        @NotNull
        @Size(min = 1, message = "At least one task identifier must be provided")
        List<@Pattern(regexp = TaskValidationConstats.UUID_PATTERN, message = TaskValidationConstats.TASK_ID_INVALID_MESSAGE) String> taskIds,

        @Schema(description = "Status that should be assigned to all provided tasks", required = true)
        @NotNull
        TaskStatus status
) {
}