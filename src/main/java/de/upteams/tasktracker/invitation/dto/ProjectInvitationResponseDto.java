package de.upteams.tasktracker.invitation.dto;

import de.upteams.tasktracker.invitation.entity.InvitationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response produced after sending a project invitation")
public record ProjectInvitationResponseDto(
        @Schema(description = "Invitation token to be used by the invitee", example = "7a1b2c3d-4e5f-6789-abcd-ef0123456789")
        String inviteToken,

        @Schema(description = "Expiration timestamp of the invitation", example = "2025-10-17T10:15:30Z")
        Instant expiresAt,

        @Schema(description = "Current status of the invitation", example = "PENDING")
        InvitationStatus status,

        @Schema(description = "Indicates whether the invitee already has an account")
        boolean registeredUser
) {
}