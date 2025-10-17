package de.upteams.tasktracker.project.dto.request;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for inviting a collaborator by email")
public record ProjectInvitationRequestDto(
        @Schema(description = "Email of the invitee", example = "user@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "Role to grant after acceptance", example = "MEMBER")
        @NotNull
        ProjectRoles role
) {
}