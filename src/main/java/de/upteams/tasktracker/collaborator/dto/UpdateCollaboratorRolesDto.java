package de.upteams.tasktracker.collaborator.dto;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;

import java.util.Set;

public record UpdateCollaboratorRolesDto(Set<ProjectRoles> newRoles) {
}
