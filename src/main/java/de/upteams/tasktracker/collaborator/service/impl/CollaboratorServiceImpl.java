package de.upteams.tasktracker.collaborator.service.impl;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.CollaboratorStatus;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.exception.CollaboratorAlreadyExistsException;
import de.upteams.tasktracker.collaborator.exception.CollaboratorNotFoundException;
import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.OwnerAlreadyExistsException;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public boolean hasUserPermission(AppUser user, UUID projectId, Collection<ProjectRoles> requiredRoles) {
        return collaboratorRepository.findByAppUserIdAndProjectId(user.getId(), projectId)
                .map(collaborator -> collaborator.getProjectRolesSet().stream()
                        .anyMatch(requiredRoles::contains))
                .orElse(false);
    }

    @Override
    public Collaborator addCollaborator(AppUser user, Project project, Set<ProjectRoles> roles) {
        project.setOwnerAssigned(true);
        if (roles.contains(ProjectRoles.OWNER)) {
            boolean hasOwner = collaboratorRepository.existsByProjectAndRole(project, ProjectRoles.OWNER);
            if (hasOwner) {
                throw new OwnerAlreadyExistsException();
            }
        }

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

        log.info("Before save: collaborator={}, roles={}", collaborator.getAppUser().getEmail(), collaborator.getProjectRolesSet());
        Collaborator savedCollaborator = collaboratorRepository.save(collaborator);
        log.info("After save: collaboratorId={}, roles={}", savedCollaborator.getId(), savedCollaborator.getProjectRolesSet());

        project.getProjectTeam().add(savedCollaborator);
        return savedCollaborator;
    }

    @Override
    public Collaborator updateCollaboratorRoles(AppUser user, Project project, Set<ProjectRoles> roles) {

        if (roles.contains(ProjectRoles.OWNER)) {
            boolean hasAnotherOwner = collaboratorRepository.existsByProjectAndRole(project, ProjectRoles.OWNER);

            Collaborator currentCollaborator = collaboratorRepository.findCollaborator(user, project)
                    .orElseThrow(CollaboratorNotFoundException::new);

            boolean isAlreadyOwner = currentCollaborator.getProjectRolesSet().contains(ProjectRoles.OWNER);

            if (hasAnotherOwner && !isAlreadyOwner) {
                throw new OwnerAlreadyExistsException();
            }
        }

        Collaborator collaborator = collaboratorRepository.findCollaborator(user, project)
                .orElseThrow(CollaboratorNotFoundException::new);

        collaborator.getProjectRolesSet().clear();
        collaborator.getProjectRolesSet().addAll(roles);
        return collaboratorRepository.save(collaborator);
    }

    @Override
    public ProjectRoles getUserRoleInProject(AppUser user, UUID projectId) {
        return collaboratorRepository.findByAppUserIdAndProjectId(user.getId(), projectId)
                .map(collaborator -> {
                    Set<ProjectRoles> roles = collaborator.getProjectRolesSet();
                    if (roles.contains(ProjectRoles.OWNER)) return ProjectRoles.OWNER;
                    if (roles.contains(ProjectRoles.ADMIN)) return ProjectRoles.ADMIN;
                    if (roles.contains(ProjectRoles.MEMBER)) return ProjectRoles.MEMBER;
                    return ProjectRoles.VIEWER;
                })
                .orElseThrow(() -> new RestApiException(HttpStatus.FORBIDDEN, "User is not a member of this project"));
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
        collaborator.setProjectRolesSet(new HashSet<>(roles));
        return collaborator;
    }


}
