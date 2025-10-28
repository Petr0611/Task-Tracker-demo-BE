package de.upteams.tasktracker.project.dto.response;

import de.upteams.tasktracker.invitation.dto.InvitationAcceptResponseDto;
import de.upteams.tasktracker.invitation.dto.ProjectInvitationDto;
import de.upteams.tasktracker.user.dto.EmployeeDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Project DTO
 *
 * @param id          Project ID
 * @param title       Project title
 * @param description Project description
 * @param owner       Author of the Project
 * @param ownerAssigned  Indicates whether the creator has been assigned as OWNER
 */
@Schema(description = "Data Transfer Object for Project entity")
public record ProjectResponseDto(
        @Schema(
                description = "Unique identifier of the Project",
                example = "7",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String id,

        @Schema(
                description = "Title of the Project",
                example = "New Website Development"
        )
        String title,

        @Schema(
                description = "Detailed description of the Project",
                example = "A Project to develop a new company website"
        )
        String description,

        @Schema(
                description = "The User who created the Project",
                accessMode = Schema.AccessMode.READ_ONLY)
        EmployeeDto owner,

        @Schema(
                description = "Flag that shows if the creator was automatically assigned as OWNER",
                example = "true"
        )
        boolean ownerAssigned,

        @Schema(description = "List of project members")
        List<MemberDto> members,

        @Schema(description = "List of pending invitations for the project")
        List<ProjectInvitationDto> invitations

) {

}
