package de.upteams.tasktracker.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Project member with role")
public record MemberDto(
        @Schema(description = "User ID", example = "42")
        java.util.UUID id,

        @Schema(description = "Display name", example = "John Smith")
        String name,

        @Schema(description = "Role in project", example = "MEMBER")
        String role
) {}
