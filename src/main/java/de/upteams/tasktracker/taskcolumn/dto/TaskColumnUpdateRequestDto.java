package de.upteams.tasktracker.taskcolumn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for updating column properties.
 */
@Schema(description = "Request DTO for updating a Task column")
public record TaskColumnUpdateRequestDto(
        @Schema(description = "New title of the column", example = "Done")
        @Length(min = 3, max = 255)
        @Pattern(
                regexp = "[A-Z][a-zA-Z1-9 ]{2,}",
                message = "Column title should be at least 3 character length and start with capital letter"
        )
        String title,

        @Schema(description = "New order index for the column", example = "3")
        @Min(0)
        Integer orderIndex
) {
}