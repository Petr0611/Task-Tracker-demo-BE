package de.upteams.tasktracker.project.dto.request;

import de.upteams.tasktracker.project.constants.ProjectValidationConstats;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Project DTO
 */
@Schema(description = "Data Transfer Object for Project entity")
public record ProjectCreateDto(

        @NotBlank(message = "Project title must not be blank")
        @Size(min = ProjectValidationConstats.TITLE_MIN,
                max = ProjectValidationConstats.TITLE_MAX)
        @Pattern(
                regexp = ProjectValidationConstats.BASE_REGEX,
                message = "Project title " + ProjectValidationConstats.BASE_MESSAGE
        )
        @Schema(
                description = "Title of the Project",
                example = "New Website Development"
        )
        String title,

        @Size(min = ProjectValidationConstats.DESCRIPTION_MIN,
                max = ProjectValidationConstats.DESCRIPTION_MAX)
        @Pattern(
                regexp = ProjectValidationConstats.BASE_REGEX,
                message = "Description " + ProjectValidationConstats.BASE_MESSAGE
        )
        @Schema(
                description = "Detailed description of the Project",
                example = "A Project to develop a new company website"
        )
        String description) {

}
