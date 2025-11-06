package de.upteams.tasktracker.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for transferring project ownership")
public record TransferOwnershipRequestDto(

        @NotBlank
        @Schema(
                description = "ID of the collaborator who should become the new owner",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        String newOwnerId

) {
}
