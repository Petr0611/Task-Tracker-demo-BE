package de.upteams.tasktracker.security.permissions;


import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.security.exception.UnexpectedPrincipalTypeException;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component("permissionEvaluator")
@RequiredArgsConstructor
public class ProjectPermissionEvaluator {

    private final CollaboratorService collaboratorService;
    private final ProjectRepository projectRepository;

    public boolean isOwner(String projectId, Authentication authentication) {
        AppUser currentUser = extractUser(authentication);
        UUID projectUUID = parseUuidOrThrow404(projectId);

        projectRepository.findById(projectUUID)
                .orElseThrow(ProjectNotFoundException::new);

        return collaboratorService.hasUserPermission(
                currentUser,
                projectUUID,
                List.of(ProjectRoles.OWNER)
        );
    }

    public boolean hasAnyRole(String projectId, Authentication authentication, List<ProjectRoles> roles) {
        AppUser currentUser = extractUser(authentication);
        UUID projectUUID = parseUuidOrThrow404(projectId);

        projectRepository.findById(projectUUID)
                .orElseThrow(ProjectNotFoundException::new);

        return collaboratorService.hasUserPermission(
                currentUser,
                projectUUID,
                roles
        );
    }

    private UUID parseUuidOrThrow404(String projectId) {
        try {
            return UUID.fromString(projectId);
        } catch (IllegalArgumentException ex) {
            throw new ProjectNotFoundException();
        }
    }


    private AppUser extractUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnexpectedPrincipalTypeException(null);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthUserDetails userDetails) {
            return userDetails.getUser();
        } else if (principal instanceof AppUser appUser) {
            return appUser;
        }
        throw new UnexpectedPrincipalTypeException(principal);
    }


}
