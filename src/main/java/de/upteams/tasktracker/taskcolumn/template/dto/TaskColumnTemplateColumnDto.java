package de.upteams.tasktracker.taskcolumn.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO describing a single column inside a template")
public record TaskColumnTemplateColumnDto(
        @Schema(description = "Identifier of the template column", example = "c4f0f540-9eb9-4742-9c86-4ad2b7a1f5e0")
        String id,

        @Schema(description = "Column title", example = "In Progress")
        String title,

        @Schema(description = "Column order index within the template", example = "1")
        Integer orderIndex
) {
}