package de.upteams.tasktracker.project.utils;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.CollaboratorStatus;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.invitation.dto.ProjectInvitationDto;
import de.upteams.tasktracker.invitation.entity.Invitation;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.MemberDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.user.util.AppUserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {AppUserMapper.class, TaskMappingService.class}
)
public interface ProjectMapper {

    @Mapping(source = "invitations", target = "invitations", qualifiedByName = "mapInvitationsToDto")
    @Mapping(source = "owner", target = "owner")
    @Mapping(source = "owner", target = "ownerAssigned", qualifiedByName = "mapOwnerAssigned")
    @Mapping(source = "projectTeam", target = "members", qualifiedByName = "mapCollaboratorsToMembers")
    ProjectResponseDto mapEntityToDto(Project entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "projectTeam", ignore = true)
    Project mapDtoToEntity(ProjectCreateDto dto);

    @Named("mapCollaboratorsToMembers")
    default List<MemberDto> mapCollaboratorsToMembers(Set<Collaborator> collaborators) {
        if (collaborators == null || collaborators.isEmpty()) {
            return List.of();
        }

        return collaborators.stream()
                .filter(c -> c.getAppUser() != null)
                .map(c -> {
                    Project project = c.getProject();

                    String role;
                    if (project != null
                            && project.getOwner() != null
                            && project.getOwner().equals(c.getAppUser())) {
                        role = ProjectRoles.OWNER.name();
                    } else {
                        role = c.getProjectRolesSet().stream()
                                .findFirst()
                                .map(Enum::name)
                                .orElse(ProjectRoles.MEMBER.name());
                    }

                    String name = resolveMemberName(c);

                    return new MemberDto(
                            c.getAppUser().getId(),
                            name,
                            role,
                            c.getAppUser().getAvatarUrl()
                    );
                })
                .toList();
    }

    default String resolveMemberName(Collaborator collaborator) {

        if (collaborator == null || collaborator.getAppUser() == null) {
            return "User";
        }

        var user = collaborator.getAppUser();

        if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            return user.getDisplayName();
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }

        return "User";
    }

    @Named("mapInvitationsToDto")
    default List<ProjectInvitationDto> mapInvitationsToDto(Set<Invitation> invitations) {
        if (invitations == null) return List.of();
        return invitations.stream()
                .map(inv -> new ProjectInvitationDto(
                        inv.getEmail(),
                        inv.getRole(),
                        switch (inv.getStatus()) {
                            case PENDING -> CollaboratorStatus.PENDING;
                            case USED -> CollaboratorStatus.ACTIVE;
                            default -> CollaboratorStatus.PENDING;
                        }
                ))
                .toList();
    }

    @Named("mapOwnerAssigned")
    default boolean mapOwnerAssigned(de.upteams.tasktracker.user.entity.AppUser owner) {
        return owner != null && owner.getId() != null;
    }

    @Named("uuidToString")
    default String mapId(UUID id) {
        return id == null ? null : id.toString();
    }
}
