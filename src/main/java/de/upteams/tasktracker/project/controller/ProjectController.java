package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.collaborator.dto.UpdateCollaboratorRolesDto;
import de.upteams.tasktracker.invitation.dto.ProjectInvitationResponseDto;
import de.upteams.tasktracker.project.controller.api.ProjectApi;
import de.upteams.tasktracker.project.dto.request.ProjectCollaboratorAddRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.request.ProjectInvitationRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectUpdateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.dto.response.RoleResponse;
import de.upteams.tasktracker.project.service.ProjectRoleService;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.entity.AppUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller that receives http-requests for various operations with Projects
 */
@RequestMapping("/api/v1/projects")
@RestController
public class ProjectController implements ProjectApi {

    private final ProjectService service;
    private final ProjectRoleService projectRoleService;
    private final ProjectService projectService;

    public ProjectController(ProjectService service, ProjectRoleService projectRoleService, ProjectService projectService) {
        this.service = service;
        this.projectRoleService = projectRoleService;
        this.projectService = projectService;
    }

    @Override
    public ProjectResponseDto save(@Valid ProjectCreateDto newProjectDto, AuthUserDetails principal) {
        return service.save(newProjectDto, principal.user());
    }

    @Override
    public ProjectResponseDto getById(String id) {
        return service.getById(id);
    }

    @Override
    @GetMapping
    public List<ProjectResponseDto> getAll(AuthUserDetails principal) {
        return projectService.findAllVisibleForUser(principal.user());
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(String id) {
        service.delete(id);
    }

    @Override
    public ProjectResponseDto update(String id, ProjectUpdateDto updateDto, AuthUserDetails principal) {
        return service.updateProject(id, updateDto, principal);
    }

    @Override
    public void addUserToProject(String id, ProjectCollaboratorAddRequestDto requestDto, AuthUserDetails principal) {
        service.addUserToProject(id, requestDto, principal.user());
    }

    @Override
    public ResponseEntity<ProjectInvitationResponseDto> inviteUserToProject(String id, ProjectInvitationRequestDto requestDto, AuthUserDetails principal) {
        ProjectInvitationResponseDto responseDto = service.inviteUserToProject(id, requestDto, principal.user());
        HttpStatus status = responseDto.registeredUser() ? HttpStatus.OK : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status).body(responseDto);
    }

    @Override
    public void updateCollaboratorRoles(String projectId,
                                        String userId,
                                        UpdateCollaboratorRolesDto dto,
                                        AuthUserDetails principal) {
        service.updateUserRolesInProject(projectId, userId, dto, principal.user());
    }

    @Override
    public RoleResponse getUserRole(String projectId, AuthUserDetails principal) {
        return projectRoleService.getUserRole(UUID.fromString(projectId), principal);
    }

    @Override
    public List<ProjectResponseDto> getMyProjects(AuthUserDetails principal) {
        return projectService.findAllVisibleForUser(principal.user());
    }
}
