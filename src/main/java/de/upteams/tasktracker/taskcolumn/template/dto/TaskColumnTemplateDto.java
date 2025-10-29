package de.upteams.tasktracker.taskcolumn.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO representing a saved task column template")
public record TaskColumnTemplateDto(
        @Schema(description = "Identifier of the template", example = "d1de5b3d-1d12-4b26-8c7a-77f5949bcfed")
        String id,

        @Schema(description = "Template name", example = "Default Kanban")
        String name,

        @Schema(description = "Identifier of the template owner", example = "1d6f0f2c-ef74-4f2e-aeea-43a6a3d96cf9")
        String ownerId,

        @Schema(description = "Ordered collection of template columns")
        List<TaskColumnTemplateColumnDto> columns
) {
}