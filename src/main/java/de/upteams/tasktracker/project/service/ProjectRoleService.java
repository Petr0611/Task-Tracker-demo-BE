package de.upteams.tasktracker.project.service;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.project.dto.response.RoleResponse;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectRoleService {

    private final CollaboratorService collaboratorService;

    public RoleResponse getUserRole(UUID projectId, AuthUserDetails principal) {
        AppUser currentUser = principal.getUser();
        ProjectRoles role = collaboratorService.getUserRoleInProject(currentUser, projectId);
        return new RoleResponse(role.name());
    }
}
