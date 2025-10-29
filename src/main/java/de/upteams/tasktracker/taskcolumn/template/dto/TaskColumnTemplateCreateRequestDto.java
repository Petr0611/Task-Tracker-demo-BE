package de.upteams.tasktracker.taskcolumn.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

@Schema(description = "Request DTO for creating a task column template from a project")
public record TaskColumnTemplateCreateRequestDto(
        @Schema(
                description = "Human readable template name",
                example = "Default Kanban"
        )
        @NotBlank(message = "Template name must not be blank")
        @Length(min = 3, max = 255, message = "Template name must be between 3 and 255 characters")
        String name
) {
}