package de.upteams.tasktracker.task.dto;

import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request DTO for moving several tasks to a single column")
public record TaskBulkMoveRequestDto(
        @Schema(description = "Identifiers of tasks that should be moved", required = true)
        @NotNull
        @Size(min = 1, message = "At least one task identifier must be provided")
        List<@Pattern(regexp = TaskValidationConstats.UUID_PATTERN, message = TaskValidationConstats.TASK_ID_INVALID_MESSAGE) String> taskIds,

        @Schema(description = "Identifier of the destination column", example = "5b70c020-07a5-43c5-b4db-5bd4f72bae94")
        @NotBlank(message = TaskValidationConstats.COLUMN_ID_INVALID_MESSAGE)
        @Pattern(regexp = TaskValidationConstats.UUID_PATTERN, message = TaskValidationConstats.COLUMN_ID_INVALID_MESSAGE)
        String targetColumnId,

        @Schema(description = "Zero-based index for the first task in the destination column. If omitted tasks will be appended.")
        @Min(value = 0, message = TaskValidationConstats.ORDER_INDEX_INVALID_MESSAGE)
        Integer startOrderIndex
) {
}