package de.upteams.tasktracker.project.service.interfaces;

import de.upteams.tasktracker.collaborator.dto.UpdateCollaboratorRolesDto;
import de.upteams.tasktracker.invitation.dto.ProjectInvitationResponseDto;
import de.upteams.tasktracker.project.dto.request.ProjectCollaboratorAddRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.request.ProjectInvitationRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectUpdateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;

/**
 * Service for various operations with Projects
 */
public interface ProjectService {

    ProjectResponseDto save(ProjectCreateDto newProjectDto, AppUser projectOwner);

    ProjectResponseDto getById(String id);

    Project getOrThrow(String id);

    List<ProjectResponseDto> getAll();

    List<ProjectResponseDto> findAllByOwner(AppUser owner);

    List<ProjectResponseDto> findAllVisibleForUser(AppUser user);

    void delete(String id);

    ProjectResponseDto updateProject(String id, ProjectUpdateDto updateDto, AuthUserDetails principal);

    void addUserToProject(String projectId, ProjectCollaboratorAddRequestDto requestDto, AppUser initiator);

    ProjectInvitationResponseDto inviteUserToProject(String projectId, ProjectInvitationRequestDto requestDto, AppUser initiator);

    void updateUserRolesInProject(String projectId, String userId, UpdateCollaboratorRolesDto dto, AppUser initiator);

    void transferOwnership(String projectId, String newOwnerId, AppUser initiator);


}
