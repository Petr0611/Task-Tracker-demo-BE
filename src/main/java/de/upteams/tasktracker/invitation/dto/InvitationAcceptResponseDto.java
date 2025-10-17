package de.upteams.tasktracker.invitation.dto;

import de.upteams.tasktracker.collaborator.entity.CollaboratorStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned when an invitation is accepted")
public record InvitationAcceptResponseDto(
        @Schema(description = "Project identifier", example = "1b4d6e70-88e0-4fd7-9bd1-7c2cb6f9a21b")
        String projectId,

        @Schema(description = "Project title", example = "New Website Development")
        String projectTitle,

        @Schema(description = "Current status of the collaborator", example = "ACTIVE")
        CollaboratorStatus collaboratorStatus
) {
}
