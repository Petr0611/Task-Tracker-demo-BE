package de.upteams.tasktracker.taskcolumn.template.dto;

import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload for applying a saved column template to a project")
public record TaskColumnTemplateApplyRequestDto(
        @Schema(description = "Identifier of the project that should receive the template", example = "7d7d2c7f-913a-4a6f-8af8-8d4f4f1c6acb")
        @NotBlank(message = TaskValidationConstats.PROJECT_ID_INVALID_MESSAGE)
        @Pattern(regexp = TaskValidationConstats.UUID_PATTERN, message = TaskValidationConstats.PROJECT_ID_INVALID_MESSAGE)
        String projectId
) {
}