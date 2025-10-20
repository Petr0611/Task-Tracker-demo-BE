package de.upteams.tasktracker.collaborator.service.interfaces;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CollaboratorService {

    boolean isUserInProject(AppUser user, Project project);

    Optional<Collaborator> getCollaborator(AppUser user, Project project);

    boolean hasUserPermission(AppUser user, Project project, ProjectRoles requiredRole);

    boolean hasUserPermission(AppUser user, Project project, Collection<ProjectRoles> requiredRoles);

    boolean hasUserPermission(AppUser user, UUID projectId, Collection<ProjectRoles> requiredRoles);

    Collaborator addCollaborator(AppUser user, Project project, Set<ProjectRoles> roles);

    Collaborator activateCollaborator(AppUser user, Project project, Set<ProjectRoles> roles);
}
