package de.upteams.tasktracker.collaborator.service.impl;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.CollaboratorStatus;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.exception.CollaboratorAlreadyExistsException;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaboratorServiceImpl implements CollaboratorService {

    private final CollaboratorRepository collaboratorRepository;

    @Override
    public boolean isUserInProject(AppUser user, Project project) {
        return getCollaborator(user, project).isPresent();
    }

    @Override
    public Optional<Collaborator> getCollaborator(AppUser user, Project project) {
        return collaboratorRepository.findCollaborator(user, project);
    }

    @Override
    public boolean hasUserPermission(AppUser user, Project project, ProjectRoles requiredRole) {
        return hasUserPermission(user, project, Collections.singletonList(requiredRole));
    }

    @Override
    public boolean hasUserPermission(AppUser user, Project project, Collection<ProjectRoles> requiredRoles) {
        return getCollaborator(user, project)
                .map(collaborator -> hasAnyRequiredRole(collaborator, requiredRoles))
                .orElse(false);
    }

    @Override
    public Collaborator addCollaborator(AppUser user, Project project, Set<ProjectRoles> roles) {
        collaboratorRepository.findCollaborator(user, project)
                .ifPresent(existing -> {
                    throw new CollaboratorAlreadyExistsException();
                });

        Collaborator collaborator = buildNewCollaborator(user, project, roles);
        collaborator.setStatus(CollaboratorStatus.ACTIVE);

        Collaborator savedCollaborator = collaboratorRepository.save(collaborator);
        project.getProjectTeam().add(savedCollaborator);
        return savedCollaborator;
    }

    @Override
    public Collaborator activateCollaborator(AppUser user, Project project, Set<ProjectRoles> roles) {
        Collaborator collaborator = collaboratorRepository.findCollaborator(user, project)
                .orElseGet(() -> buildNewCollaborator(user, project, Set.of()));

        collaborator.getProjectRolesSet().addAll(roles);
        collaborator.setStatus(CollaboratorStatus.ACTIVE);

        Collaborator savedCollaborator = collaboratorRepository.save(collaborator);
        project.getProjectTeam().add(savedCollaborator);
        return savedCollaborator;
    }

    private boolean hasAnyRequiredRole(Collaborator collaborator, Collection<ProjectRoles> requiredRoles) {
        return collaborator.getProjectRolesSet()
                .stream()
                .anyMatch(requiredRoles::contains);
    }

    private Collaborator buildNewCollaborator(AppUser user, Project project, Set<ProjectRoles> roles) {
        Collaborator collaborator = new Collaborator();
        collaborator.setAppUser(user);
        collaborator.setProject(project);
        collaborator.getProjectRolesSet().addAll(roles);
        return collaborator;
    }

}
