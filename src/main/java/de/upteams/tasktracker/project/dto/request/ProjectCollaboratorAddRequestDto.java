package de.upteams.tasktracker.project.dto.request;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * Request DTO for adding a collaborator to a project.
 *
 * @param userId Identifier of the user to add to the project team
 * @param roles  Set of roles that should be granted to the collaborator
 */
@Schema(description = "Payload to add a collaborator to a project")
public record ProjectCollaboratorAddRequestDto(
        @Schema(
                description = "Identifier of the user that should be added to the project",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @NotBlank
        String userId,

        @Schema(
                description = "Set of project roles that will be assigned to the collaborator"
        )
        @NotEmpty
        Set<ProjectRoles> roles
) {
}
