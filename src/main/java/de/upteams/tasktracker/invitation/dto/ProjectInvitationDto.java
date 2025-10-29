package de.upteams.tasktracker.invitation.dto;

import de.upteams.tasktracker.collaborator.entity.CollaboratorStatus;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Invitation info for displaying in project list")
public record ProjectInvitationDto(
        @Schema(description = "Email of the invited user", example = "user@example.com")
        String email,

        @Schema(description = "Role assigned upon acceptance", example = "MEMBER")
        ProjectRoles role,

        @Schema(description = "Current status of the invitation", example = "PENDING")
        CollaboratorStatus collaboratorStatus
) {}
