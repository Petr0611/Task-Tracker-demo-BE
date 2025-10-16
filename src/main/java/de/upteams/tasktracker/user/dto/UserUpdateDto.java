package de.upteams.tasktracker.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for updating user's profile
 *
 * <p>This DTO contains only the fields that a user is allowed to update in their profile.
 * It is used in profile edit endpoints.</p>
 */
@Schema(description = "DTO for updating user's profile")
public record UserUpdateDto(

        @Schema(
                description = "User's display name",
                example = "Homer Simpson"
        )
        String displayName,

        @Schema(
                description = "User's job position",
                example = "Developer"
        )
        String position,

        @Schema(
                description = "Department or team of the user",
                example = "Engineering"
        )
        String department,

        @Schema(
                description = "URL of user's avatar image",
                example = "https://example.com/avatar/homer.png"
        )
        String avatarUrl,

        @Schema(
                description = "Short biography or info about the user",
                example = "Loves donuts and coding"
        )
        String bio
) {}