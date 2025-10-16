package de.upteams.tasktracker.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for updating an existing Project")
public record ProjectUpdateDto (
        @Schema(description = "New title of the Project",
                example = "Updated Project Title")
        String title,

        @Schema(description = "New description of the Project",
                example = "Updated project details")
        String description

) {}

