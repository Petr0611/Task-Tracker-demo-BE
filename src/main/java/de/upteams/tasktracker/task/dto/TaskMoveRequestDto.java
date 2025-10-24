package de.upteams.tasktracker.task.dto;

import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for moving a task within or between columns.
 *
 * @param columnId   Identifier of the destination column. If omitted, task stays in the current column.
 * @param orderIndex Zero-based position in the destination column.
 */
@Schema(description = "Request DTO for moving a Task between columns")
public record TaskMoveRequestDto(
        @Schema(
                description = "Identifier of the destination column. If not set, the current column is used.",
                example = "5b70c020-07a5-43c5-b4db-5bd4f72bae94"
        )
        @Pattern(regexp = TaskValidationConstats.UUID_PATTERN, message = TaskValidationConstats.COLUMN_ID_INVALID_MESSAGE)
        String columnId,

        @Schema(
                description = "Zero-based order index within the destination column",
                example = "1"
        )
        @NotNull
        @Min(value = 0, message = TaskValidationConstats.ORDER_INDEX_INVALID_MESSAGE)
        Integer orderIndex
) {
}