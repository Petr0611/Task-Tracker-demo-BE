package de.upteams.tasktracker.task.service.impl;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import de.upteams.tasktracker.task.dto.TaskCreateRequestDto;
import de.upteams.tasktracker.task.dto.TaskDto;
import de.upteams.tasktracker.task.dto.TaskUpdateRequestDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.exception.TaskNotFoundException;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.taskcolumn.entity.TaskColumn;
import de.upteams.tasktracker.taskcolumn.service.interfaces.TaskColumnService;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for various operations with Tasks
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final String COLUMN_PROJECT_MISMATCH_MESSAGE = "Column does not belong to the specified project";

    private final TaskRepository repository;
    private final TaskMappingService mappingService;
    private final ProjectService projectService;
    private final CollaboratorService collaboratorService;
    private final TaskColumnService columnService;

    @Override
    public TaskDto save(final String projectId, final TaskCreateRequestDto newTaskDto, final AppUser creator) {
        final Project project = getProjectOrThrow(projectId);
        enforceProjectAccess(project, creator);

        final TaskColumn column = getColumnOrThrow(newTaskDto.columnId());
        ensureColumnBelongsToProject(column, project);

        final Task entity = new Task();
        entity.setTitle(newTaskDto.title());
        entity.setDescription(newTaskDto.description());
        entity.setProject(project);
        entity.setColumn(column);

        return mappingService.mapEntityToDto(repository.save(entity));
    }

    @Override
    public TaskDto getById(final String id, final AppUser requester) {
        final Task task = getOrThrow(id);
        enforceProjectAccess(task.getProject(), requester);
        return mappingService.mapEntityToDto(task);
    }

    @Override
    public Task getOrThrow(String id) {
        return findById(id)
                .orElseThrow(TaskNotFoundException::new);
    }

    @Override
    public Optional<Task> findById(String id) {
        final UUID taskId = parseUuid(id, TaskValidationConstats.TASK_ID_INVALID_MESSAGE);
        return repository.findById(taskId);
    }

    @Override
    public List<TaskDto> getAll(final String projectId, final AppUser authUser) {
        final Project project = getProjectOrThrow(projectId);
        enforceProjectAccess(project, authUser);
        return repository
                .findByProject(project)
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public List<TaskDto> getAllByColumn(String columnId, AppUser authUser) {
        final TaskColumn column = getColumnOrThrow(columnId);
        enforceProjectAccess(column.getProject(), authUser);
        return repository
                .findByColumn(column)
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public void delete(final String id, final AppUser changer) {
        final Task existedTask = getOrThrow(id);
        enforceTaskManagementPermission(existedTask.getProject(), changer);
        repository.delete(existedTask);
    }

    @Override
    public TaskDto updateTask(final String id, final TaskUpdateRequestDto updateDto, final AppUser changer) {
        final Task task = getOrThrow(id);
        enforceTaskManagementPermission(task.getProject(), changer);

        if (updateDto.title() != null && !updateDto.title().isBlank()) {
            task.setTitle(updateDto.title());
        }

        if (updateDto.description() != null && !updateDto.description().isBlank()) {
            task.setDescription(updateDto.description());
        }

        if (updateDto.columnId() != null && !updateDto.columnId().isBlank()) {
            final TaskColumn newColumn = getColumnOrThrow(updateDto.columnId());
            ensureColumnBelongsToProject(newColumn, task.getProject());
            task.setColumn(newColumn);
            task.setProject(newColumn.getProject());
        }

        final Task updated = repository.save(task);
        return mappingService.mapEntityToDto(updated);
    }

    private void enforceProjectAccess(final Project project, final AppUser user) {
        if (isProjectOwner(project, user)) {
            return;
        }

        final boolean userInProject = collaboratorService.isUserInProject(user, project);
        if (!userInProject) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }
    }

    private void enforceTaskManagementPermission(final Project project, final AppUser user) {
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

    private boolean isProjectOwner(final Project project, final AppUser user) {
        return project.getOwner() != null && project.getOwner().equals(user);
    }

    private Project getProjectOrThrow(final String projectId) {
        try {
            return projectService.getOrTrow(projectId);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, TaskValidationConstats.PROJECT_ID_INVALID_MESSAGE);
        }
    }

    private TaskColumn getColumnOrThrow(final String columnId) {
        return columnService.getOrThrow(columnId);
    }

    private void ensureColumnBelongsToProject(TaskColumn column, Project project) {
        if (!column.getProject().equals(project)) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, COLUMN_PROJECT_MISMATCH_MESSAGE);
        }
    }

    private UUID parseUuid(final String rawId, final String errorMessage) {
        try {
            return UUID.fromString(rawId);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }
}