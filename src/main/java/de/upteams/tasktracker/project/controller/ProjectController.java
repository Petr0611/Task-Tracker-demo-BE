package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.collaborator.dto.UpdateCollaboratorRolesDto;
import de.upteams.tasktracker.invitation.dto.ProjectInvitationResponseDto;
import de.upteams.tasktracker.project.controller.api.ProjectApi;
import de.upteams.tasktracker.project.dto.request.ProjectCollaboratorAddRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.request.ProjectInvitationRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectUpdateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller that receives http-requests for various operations with Projects
 */
@RequestMapping("/api/v1/projects")
@RestController
public class ProjectController implements ProjectApi {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @Override
    public ProjectResponseDto save(@Valid ProjectCreateDto newProjectDto, AuthUserDetails principal) {
        return service.save(newProjectDto, principal.user());
    }

    @Override
    public ProjectResponseDto getById(String id) {
        return service.getById(id);
    }

//    @Override
//    public List<ProjectResponseDto> getAll() {
//        return service.getAll();
//    }

    @Override
    public List<ProjectResponseDto> getAll(AuthUserDetails principal) {
        return service.findAllByOwner(principal.user());
    }


    @Override
    public void deleteById(String id) {
        service.delete(id);
    }

    @Override
    public ProjectResponseDto update(String id, ProjectUpdateDto updateDto) {
        return service.updateProject(id, updateDto);
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
}
