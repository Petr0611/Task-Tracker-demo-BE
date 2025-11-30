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
import de.upteams.tasktracker.project.persistence.ProjectRepository;
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
    private final ProjectRepository projectRepository;

    @Override
    public boolean isUserInProject(AppUser user, Project project) {
        if (project.getOwner() != null && project.getOwner().equals(user)) {
            return true;
        }

        log.info("Checking collaborator: userId={}, projectId={}", user.getId(), project.getId());

        return collaboratorRepository.findCollaborator(user.getId(), project.getId())
                .map(collaborator -> {
                    log.info("Found collaborator: status={}, roles={}", collaborator.getStatus(), collaborator.getProjectRolesSet());
                    return collaborator.getStatus() == CollaboratorStatus.ACTIVE || collaborator.getStatus() == CollaboratorStatus.PENDING;
                })
                .orElse(false);

    }


    @Override
    public Optional<Collaborator> getCollaborator(AppUser user, Project project) {
        return collaboratorRepository.findCollaborator(user.getId(), project.getId());
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

        collaboratorRepository.findCollaborator(user.getId(), project.getId())
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
        if (roles.contains(ProjectRoles.OWNER)) {
            boolean hasOwner = collaboratorRepository.existsByProjectAndRole(project, ProjectRoles.OWNER);

            boolean isAlreadyOwner = collaboratorRepository.findCollaborator(user.getId(), project.getId())
                    .map(c -> c.getProjectRolesSet().contains(ProjectRoles.OWNER))
                    .orElse(false);

            if (hasOwner && !isAlreadyOwner) {
                throw new OwnerAlreadyExistsException();
            }
        }

        Collaborator collaborator = collaboratorRepository.findCollaborator(user.getId(), project.getId())
                .orElseGet(() -> buildNewCollaborator(user, project, Set.of()));

        collaborator.getProjectRolesSet().addAll(roles);
        collaborator.setStatus(CollaboratorStatus.ACTIVE);

        Collaborator savedCollaborator = collaboratorRepository.save(collaborator);

        project.getProjectTeam().add(savedCollaborator);
        return savedCollaborator;
    }

    @Override
    public Collaborator updateCollaboratorRoles(AppUser user, Project project, Set<ProjectRoles> roles) {

        if (roles.contains(ProjectRoles.OWNER)) {
            boolean hasAnotherOwner = collaboratorRepository.existsByProjectAndRole(project, ProjectRoles.OWNER);

            Collaborator currentCollaborator = collaboratorRepository.findCollaborator(user.getId(), project.getId())
                    .orElseThrow(CollaboratorNotFoundException::new);

            boolean isAlreadyOwner = currentCollaborator.getProjectRolesSet().contains(ProjectRoles.OWNER);

            if (hasAnotherOwner && !isAlreadyOwner) {
                throw new OwnerAlreadyExistsException();
            }
        }

        Collaborator collaborator = collaboratorRepository.findCollaborator(user.getId(), project.getId())
                .orElseThrow(CollaboratorNotFoundException::new);

        collaborator.getProjectRolesSet().clear();
        collaborator.getProjectRolesSet().addAll(roles);
        return collaboratorRepository.save(collaborator);
    }

    @Override
    public ProjectRoles getUserRoleInProject(AppUser user, UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RestApiException(HttpStatus.NOT_FOUND, "Project not found"));

        if (project.getOwner() != null && project.getOwner().equals(user)) {
            return ProjectRoles.OWNER;
        }

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

    @Override
    public boolean projectHasRole(Project project, ProjectRoles projectRoles) {
        return collaboratorRepository.existsByProjectAndRole(project, projectRoles);
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
