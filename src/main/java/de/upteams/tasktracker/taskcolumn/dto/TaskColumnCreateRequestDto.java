package de.upteams.tasktracker.taskcolumn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for creating a new column inside a Project board.
 */
@Schema(description = "Request DTO for creating a Task column")
public record TaskColumnCreateRequestDto(
        @Schema(description = "Title of the column", example = "To Do")
        @NotBlank
        @Length(min = 3, max = 255)
        @Pattern(
                regexp = "[A-Z][a-zA-Z1-9 ]{2,}",
                message = "Column title should be at least 3 character length and start with capital letter"
        )
        String title,

        @Schema(
                description = "Order index for the new column. If omitted, it will be appended to the end.",
                example = "2"
        )
        @Min(0)
        Integer orderIndex
) {
}