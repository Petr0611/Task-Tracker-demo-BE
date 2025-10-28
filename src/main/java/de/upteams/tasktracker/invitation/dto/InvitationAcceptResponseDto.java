package de.upteams.tasktracker.invitation.dto;

import de.upteams.tasktracker.collaborator.entity.CollaboratorStatus;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned when an invitation is accepted")
public record InvitationAcceptResponseDto(
        @Schema(description = "Project ID", example = "1b4d6e70-88e0-4fd7-9bd1-7c2cb6f9a21b")
        String projectId,

        @Schema(description = "Project title", example = "New Website")
        String projectTitle,

        @Schema(description = "Current status of the collaborator", example = "ACTIVE")
        CollaboratorStatus collaboratorStatus,

        @Schema(description = "Role assigned to the user in the project", example = "ADMIN")
        ProjectRoles role
) {}
