package de.upteams.tasktracker.taskcolumn.service.impl;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnCreateRequestDto;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnDto;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnUpdateRequestDto;
import de.upteams.tasktracker.taskcolumn.entity.TaskColumn;
import de.upteams.tasktracker.taskcolumn.exception.TaskColumnNotFoundException;
import de.upteams.tasktracker.taskcolumn.persistence.TaskColumnRepository;
import de.upteams.tasktracker.taskcolumn.service.interfaces.TaskColumnService;
import de.upteams.tasktracker.taskcolumn.utils.TaskColumnMappingService;
import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for managing Task columns.
 */
@Service
@RequiredArgsConstructor
public class TaskColumnServiceImpl implements TaskColumnService {

    private static final String BASE_COLUMN_DELETE_FORBIDDEN_MESSAGE = "Base columns cannot be deleted";
    private static final String COLUMN_MANAGE_FORBIDDEN_MESSAGE = "Only project owner can manage columns";

    private final TaskColumnRepository repository;
    private final TaskColumnMappingService mappingService;
    private final ProjectService projectService;
    private final CollaboratorService collaboratorService;

    @Override
    @Transactional
    public TaskColumnDto create(String projectId, TaskColumnCreateRequestDto requestDto, AppUser creator) {
        final Project project = getProjectOrThrow(projectId);
        enforceProjectAccess(project, creator);

        final TaskColumn column = new TaskColumn();
        column.setTitle(requestDto.title());
        column.setProject(project);
        column.setOrderIndex(resolveOrderIndex(project, requestDto.orderIndex()));

        return mappingService.mapEntityToDto(repository.save(column));
    }

    @Override
    @Transactional(readOnly = true)
    public TaskColumnDto getById(String id, AppUser requester) {
        final TaskColumn column = getOrThrow(id);
        enforceProjectAccess(column.getProject(), requester);
        return mappingService.mapEntityToDto(column);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskColumnDto> getAll(String projectId, AppUser authUser) {
        final Project project = getProjectOrThrow(projectId);
        enforceProjectAccess(project, authUser);
        return mappingService.mapEntitiesToDtos(repository.findAllByProjectOrderByOrderIndexAsc(project));
    }

    @Override
    @Transactional
    public TaskColumnDto update(String id, TaskColumnUpdateRequestDto requestDto, AppUser changer) {
        final TaskColumn column = getOrThrow(id);
        enforceTaskManagementPermission(column.getProject(), changer);

        if (requestDto.title() != null && !requestDto.title().isBlank()) {
            column.setTitle(requestDto.title());
        }
        if (requestDto.orderIndex() != null) {
            column.setOrderIndex(requestDto.orderIndex());
        }
        if (requestDto.baseColumn() != null) {
            enforceOwnerPermission(column.getProject(), changer);
            column.setBaseColumn(requestDto.baseColumn());
        }

        return mappingService.mapEntityToDto(repository.save(column));
    }

    @Override
    @Transactional
    public void delete(String id, AppUser changer) {
        final TaskColumn column = getOrThrow(id);
        enforceOwnerPermission(column.getProject(), changer);

        if (column.isBaseColumn()) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, BASE_COLUMN_DELETE_FORBIDDEN_MESSAGE);
        }
        repository.delete(column);
    }

    @Override
    public TaskColumn getOrThrow(String id) {
        return findById(id).orElseThrow(TaskColumnNotFoundException::new);
    }

    @Override
    public Optional<TaskColumn> findById(String id) {
        final UUID columnId = parseUuid(id, TaskValidationConstats.COLUMN_ID_INVALID_MESSAGE);
        return repository.findById(columnId);
    }

    private Project getProjectOrThrow(String projectId) {
        try {
            return projectService.getOrTrow(projectId);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, TaskValidationConstats.PROJECT_ID_INVALID_MESSAGE);
        }
    }

    private void enforceProjectAccess(Project project, AppUser user) {
        if (isProjectOwner(project, user)) {
            return;
        }

        final boolean userInProject = collaboratorService.isUserInProject(user, project);
        if (!userInProject) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }
    }

    private void enforceTaskManagementPermission(Project project, AppUser user) {
        if (isProjectOwner(project, user)) {
            return;
        }

        final boolean hasPermission = collaboratorService.hasUserPermission(
                user,
                project,
                List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }
    }

    private void enforceOwnerPermission(Project project, AppUser user) {
        if (!isProjectOwner(project, user)) {
            throw new RestApiException(HttpStatus.FORBIDDEN, COLUMN_MANAGE_FORBIDDEN_MESSAGE);
        }
    }

    private boolean isProjectOwner(Project project, AppUser user) {
        return project.getOwner() != null && project.getOwner().equals(user);
    }

    private int resolveOrderIndex(Project project, Integer requestedOrder) {
        if (requestedOrder != null) {
            return requestedOrder;
        }
        final Integer maxOrder = repository.findMaxOrderIndex(project);
        return (maxOrder == null ? -1 : maxOrder) + 1;
    }

    private UUID parseUuid(String rawId, String errorMessage) {
        try {
            return UUID.fromString(rawId);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }
}