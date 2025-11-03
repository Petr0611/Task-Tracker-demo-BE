package de.upteams.tasktracker.user.dto.response;

import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO returned when fetching user data.
 */
@Schema(description = "User data returned by the API")
public record UserResponseDto(
        @Schema(
                description = "User's ID",
                example     = "e02414e8-f4f7-4b4c-9045-f295cbe3f28a"
        )
        String id,
        @Schema(
                description = "User's email address",
                example     = "homer@simpsons.com"
        )
        String email,

        @Schema(
                description = "Role assigned to the user",
                example     = "ROLE_USER",
                accessMode  = Schema.AccessMode.READ_ONLY
        )
        String role,

        @Schema(
                description = "Confirmation status of the user account",
                example     = "UNCONFIRMED",
                accessMode  = Schema.AccessMode.READ_ONLY
        )
        ConfirmationStatus confirmationStatus,

        @Schema(
                description = "Display name of the user",
                example     = "Homer Simpson"
        )
        String displayName,

        @Schema(
                description = "User's job position",
                example     = "Developer"
        )
        String position,

        @Schema(
                description = "Department or team of the user",
                example     = "Engineering"
        )
        String department,

        @Schema(
                description = "URL of the user's avatar",
                example     = "https://example.com/avatar/homer.png"
        )
        String avatarUrl,

        @Schema(
                description = "Short biography or info about the user",
                example     = "Loves donuts and coding"
        )
        String bio
) {}
