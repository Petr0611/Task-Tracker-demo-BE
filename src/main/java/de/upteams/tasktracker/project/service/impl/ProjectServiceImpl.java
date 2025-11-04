package de.upteams.tasktracker.project.service.impl;

import de.upteams.tasktracker.collaborator.dto.UpdateCollaboratorRolesDto;
import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.invitation.dto.ProjectInvitationResponseDto;
import de.upteams.tasktracker.invitation.entity.InvitationStatus;
import de.upteams.tasktracker.invitation.service.interfaces.InvitationService;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.request.ProjectUpdateDto;
import de.upteams.tasktracker.project.dto.request.ProjectCollaboratorAddRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectInvitationRequestDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.project.utils.ProjectMapper;
import de.upteams.tasktracker.security.permissions.ProjectPermissionEvaluator;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repository;
    private final ProjectMapper mappingService;
    private final CollaboratorService collaboratorService;
    private final InvitationService invitationService;
    private final UserService userService;
    private final ProjectPermissionEvaluator permissionEvaluator;

    @Transactional
    @Override
    public ProjectResponseDto save(ProjectCreateDto newProjectDto, AppUser projectOwner) {
        Project project = mappingService.mapDtoToEntity(newProjectDto);
        project.setOwner(projectOwner);
        Project savedProject = repository.save(project);

        collaboratorService.addCollaborator(
                projectOwner,
                savedProject,
                Set.of(ProjectRoles.OWNER)
        );

        return mappingService.mapEntityToDto(savedProject);
    }

    @Override
    public ProjectResponseDto getById(String id) {
        return mappingService.mapEntityToDto(getOrTrow(id));
    }

    @Override
    public Project getOrTrow(String id) {
        return repository.findById(UUID.fromString(id))
                .orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public List<ProjectResponseDto> getAll() {
        return repository.findAll()
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public List<ProjectResponseDto> findAllByOwner(AppUser owner) {
        return repository.findAllByOwner(owner)
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public List<ProjectResponseDto> findAllVisibleForUser(AppUser user) {
        return repository.findAllVisibleForUser(user)
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }


    @Override
    public void delete(String id) {
        repository.deleteById(UUID.fromString(id));
    }

    @Override
    public ProjectResponseDto updateProject(String id, ProjectUpdateDto updateDTO, AuthUserDetails principal) {
        Project project = getOrTrow(id);

        if (!permissionEvaluator.hasAnyRole(
                id,
                SecurityContextHolder.getContext().getAuthentication(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN))
        ) {
            throw new AccessDeniedException("You do not have permission to update this project");
        }

        if (updateDTO.title() != null && !updateDTO.title().isBlank()) {
            project.setTitle(updateDTO.title());
        }
        if (updateDTO.description() != null && !updateDTO.description().isBlank()) {
            project.setDescription(updateDTO.description());
        }

        return mappingService.mapEntityToDto(repository.save(project));
    }

    @Override
    public void addUserToProject(String projectId, ProjectCollaboratorAddRequestDto requestDto, AppUser initiator) {
        Project project = getOrTrow(projectId);
        enforceTeamManagementPermission(project, initiator);

        AppUser userToAdd = userService.getByIdOrThrow(requestDto.userId());
        Set<ProjectRoles> roles = EnumSet.copyOf(requestDto.roles());
        collaboratorService.addCollaborator(userToAdd, project, roles);
    }

    @Override
    public ProjectInvitationResponseDto inviteUserToProject(String projectId, ProjectInvitationRequestDto requestDto, AppUser initiator) {
        Project project = getOrTrow(projectId);
        enforceTeamManagementPermission(project, initiator);

        InvitationService.InvitationCreationResult creationResult = invitationService.createOrRenewInvitation(
                project,
                requestDto.email(),
                requestDto.role()
        );

        return new ProjectInvitationResponseDto(
                creationResult.invitation().getInviteToken(),
                creationResult.invitation().getExpiresAt(),
                creationResult.invitation().getStatus(),
                creationResult.registeredUser()
        );
    }

    @Override
    public void updateUserRolesInProject(String projectId, String userId, UpdateCollaboratorRolesDto dto, AppUser initiator) {
        Project project = getOrTrow(projectId);
        enforceTeamManagementPermission(project, initiator);

        AppUser userToUpdate = userService.getByIdOrThrow(userId);
        collaboratorService.updateCollaboratorRoles(userToUpdate, project, EnumSet.copyOf(dto.newRoles()));
    }

    private void enforceTeamManagementPermission(Project project, AppUser initiator) {
        boolean isOwner = project.getOwner().equals(initiator);
        if (isOwner) return;

        boolean hasPermission = collaboratorService.hasUserPermission(
                initiator,
                project,
                List.of(ProjectRoles.ADMIN, ProjectRoles.OWNER)
        );

        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no rights to manage project team");
        }
    }
}
