package de.upteams.tasktracker.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

@Schema(description = "DTO for updating an existing Project")
public record ProjectUpdateDto(
        @Schema(description = "New title of the Project",
                example = "Updated Project Title")
        @Length(min = 3, max = 155, message = "Title must be between 3 and 155 characters long")
        @Pattern(
                regexp = "[A-Z][a-zA-Z1-9,.%:?&!$;*() ]{2,}",
                message = "Title must start with a capital letter"
        )
        String title,

        @Schema(description = "New description of the Project",
                example = "Updated project details")
        @Length(min = 3, max = 255, message = "Description must be at least 3 characters long")
        @Pattern(
                regexp = "[A-Z][a-zA-Z1-9,.%:?&!$;*() ]{2,}",
                message = "Description must start with a capital letter"
        )
        String description

) {
}

