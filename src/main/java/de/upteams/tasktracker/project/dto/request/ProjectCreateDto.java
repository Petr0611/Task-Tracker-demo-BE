package de.upteams.tasktracker.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Project DTO
 */
@Schema(description = "Data Transfer Object for Project entity")
public record ProjectCreateDto(

        @NotBlank(message = "Project title must not be blank")
        @Size(max = 100, message = "Project title must be at most 100 characters")
        @Schema(
                description = "Title of the Project",
                example = "New Website Development"
        )
        String title,

        @Size(max = 500, message = "Description must be at most 500 characters")
        @Schema(
                description = "Detailed description of the Project",
                example = "A Project to develop a new company website"
        )
        String description) {

}
