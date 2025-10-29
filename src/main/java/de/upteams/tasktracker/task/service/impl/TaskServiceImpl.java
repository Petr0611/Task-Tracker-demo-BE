package de.upteams.tasktracker.task.service.impl;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import de.upteams.tasktracker.task.dto.*;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.entity.TaskStatus;
import de.upteams.tasktracker.task.exception.TaskNotFoundException;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.task.persistence.TaskSpecifications;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.taskcolumn.entity.TaskColumn;
import de.upteams.tasktracker.taskcolumn.service.interfaces.TaskColumnService;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for various operations with Tasks
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final String COLUMN_PROJECT_MISMATCH_MESSAGE = "Column does not belong to the specified project";
    private static final String TASK_PROJECT_NOT_ASSIGNED_MESSAGE = "Task is not associated with any project";
    private static final String TASKS_NOT_PROVIDED_MESSAGE = "At least one task must be provided";

    private final TaskRepository repository;
    private final TaskMappingService mappingService;
    private final ProjectService projectService;
    private final CollaboratorService collaboratorService;
    private final TaskColumnService columnService;

    @Override
    @Transactional
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
        entity.setOrderIndex(getNextOrderIndex(column));
        entity.setStatus(Optional.ofNullable(newTaskDto.status()).orElse(TaskStatus.NEW));
        applyDueDate(entity, newTaskDto.dueDate());

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
    public List<TaskDto> getAll(final String projectId,
                                final TaskFilterParams filterParams,
                                final AppUser authUser) {
        final Project project = getProjectOrThrow(projectId);
        enforceProjectAccess(project, authUser);
        final TaskFilterParams safeFilter = filterParams == null
                ? new TaskFilterParams(null, null, null, null)
                : filterParams;

        final UUID executorId = resolveExecutorId(safeFilter.executorId());

        Specification<Task> specification = TaskSpecifications.belongsToProject(project);

        if (safeFilter.status() != null) {
            specification = specification.and(TaskSpecifications.hasStatus(safeFilter.status()));
        }

        if (executorId != null) {
            specification = specification.and(TaskSpecifications.hasExecutor(executorId));
        }

        if (safeFilter.dueBefore() != null) {
            specification = specification.and(TaskSpecifications.dueBefore(safeFilter.dueBefore()));
        }

        final Sort sort = resolveSort(safeFilter.sortBy());

        return repository
                .findAll(specification, sort)
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
    @Transactional
    public void delete(final String id, final AppUser changer) {
        final Task existedTask = getOrThrow(id);
        enforceTaskManagementPermission(existedTask.getProject(), changer);
        final TaskColumn column = existedTask.getColumn();
        repository.delete(existedTask);
        repository.flush();
        reindexColumn(column);
    }

    @Override
    @Transactional
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
            if (!newColumn.equals(task.getColumn())) {
                moveTaskToPosition(task, newColumn, Integer.MAX_VALUE);
            }
        }

        if (updateDto.status() != null) {
            task.setStatus(updateDto.status());
        }

        if (updateDto.dueDate() != null && !Objects.equals(task.getDueDate(), updateDto.dueDate())) {
            applyDueDate(task, updateDto.dueDate());
        }

        final Task updated = repository.save(task);
        return mappingService.mapEntityToDto(updated);
    }

    @Override
    @Transactional
    public TaskDto moveTask(String id, TaskMoveRequestDto moveDto, AppUser changer) {
        final Task task = getOrThrow(id);
        enforceTaskManagementPermission(task.getProject(), changer);

        final TaskColumn targetColumn = moveDto.columnId() != null && !moveDto.columnId().isBlank()
                ? getColumnOrThrow(moveDto.columnId())
                : task.getColumn();
        ensureColumnBelongsToProject(targetColumn, task.getProject());

        if (moveDto.orderIndex() == null) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, TaskValidationConstats.ORDER_INDEX_INVALID_MESSAGE);
        }

        final int targetIndex = moveDto.orderIndex();
        if (targetIndex < 0) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, TaskValidationConstats.ORDER_INDEX_INVALID_MESSAGE);
        }

        moveTaskToPosition(task, targetColumn, targetIndex);
        return mappingService.mapEntityToDto(task);
    }

    @Override
    @Transactional
    public List<TaskDto> bulkMoveTasks(TaskBulkMoveRequestDto requestDto, AppUser changer) {
        final List<Task> tasksToMove = resolveTasksInRequestedOrder(requestDto.taskIds());
        if (tasksToMove.isEmpty()) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, TASKS_NOT_PROVIDED_MESSAGE);
        }

        final TaskColumn targetColumn = getColumnOrThrow(requestDto.targetColumnId());
        final Project targetProject = targetColumn.getProject();
        ensureColumnBelongsToProject(targetColumn, targetProject);

        enforceTaskManagementPermission(targetProject, changer);
        enforceTaskManagementPermissionForTasks(tasksToMove, changer);

        final List<Task> targetColumnTasks = new ArrayList<>(repository.findAllByColumnOrderByOrderIndexAsc(targetColumn));
        final Map<UUID, List<Task>> sourceColumns = detachTasksFromSourceColumns(tasksToMove, targetColumn);

        final Set<UUID> movingTaskIds = tasksToMove.stream()
                .map(Task::getId)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        targetColumnTasks.removeIf(task -> movingTaskIds.contains(task.getId()));

        int insertionIndex = requestDto.startOrderIndex() != null ? requestDto.startOrderIndex() : targetColumnTasks.size();
        insertionIndex = Math.max(insertionIndex, 0);
        insertionIndex = Math.min(insertionIndex, targetColumnTasks.size());

        for (Task task : tasksToMove) {
            task.setColumn(targetColumn);
            task.setProject(targetProject);
            targetColumnTasks.add(insertionIndex, task);
            insertionIndex++;
        }

        reindexTasks(targetColumnTasks);
        repository.saveAll(targetColumnTasks);

        for (List<Task> tasks : sourceColumns.values()) {
            if (tasks.isEmpty()) {
                continue;
            }
            reindexTasks(tasks);
            repository.saveAll(tasks);
        }

        return tasksToMove.stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    @Transactional
    public List<TaskDto> bulkUpdateStatus(TaskBulkStatusUpdateRequestDto requestDto, AppUser changer) {
        final List<Task> tasksToUpdate = resolveTasksInRequestedOrder(requestDto.taskIds());
        if (tasksToUpdate.isEmpty()) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, TASKS_NOT_PROVIDED_MESSAGE);
        }

        enforceTaskManagementPermissionForTasks(tasksToUpdate, changer);

        for (Task task : tasksToUpdate) {
            task.setStatus(requestDto.status());
        }

        repository.saveAll(tasksToUpdate);

        return tasksToUpdate.stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    private List<Task> resolveTasksInRequestedOrder(List<String> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return List.of();
        }

        final Map<UUID, Task> orderedTasks = new LinkedHashMap<>();
        for (String rawId : taskIds) {
            final Task task = getOrThrow(rawId);
            orderedTasks.putIfAbsent(task.getId(), task);
        }

        return new ArrayList<>(orderedTasks.values());
    }

    private void enforceTaskManagementPermissionForTasks(List<Task> tasks, AppUser user) {
        final Set<Project> projects = new LinkedHashSet<>();

        for (Task task : tasks) {
            final Project project = task.getProject();
            if (project == null) {
                throw new RestApiException(HttpStatus.BAD_REQUEST, TASK_PROJECT_NOT_ASSIGNED_MESSAGE);
            }

            if (projects.add(project)) {
                enforceTaskManagementPermission(project, user);
            }
        }
    }

    private Map<UUID, List<Task>> detachTasksFromSourceColumns(List<Task> tasks, TaskColumn targetColumn) {
        final Map<UUID, List<Task>> sourceColumns = new HashMap<>();

        for (Task task : tasks) {
            final TaskColumn sourceColumn = task.getColumn();
            if (sourceColumn == null || sourceColumn.equals(targetColumn)) {
                continue;
            }

            final UUID sourceColumnId = sourceColumn.getId();
            final List<Task> snapshot = sourceColumns.computeIfAbsent(sourceColumnId, id ->
                    new ArrayList<>(repository.findAllByColumnOrderByOrderIndexAsc(sourceColumn))
            );
            snapshot.removeIf(existing -> existing.getId().equals(task.getId()));
        }

        return sourceColumns;
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
        if (column.getProject() == null || project == null) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, COLUMN_PROJECT_MISMATCH_MESSAGE);
        }

        final UUID columnProjectId = column.getProject().getId();
        final UUID projectId = project.getId();

        if (columnProjectId == null || projectId == null || !columnProjectId.equals(projectId)) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, COLUMN_PROJECT_MISMATCH_MESSAGE);
        }
    }

    private int getNextOrderIndex(TaskColumn column) {
        final Integer maxOrderIndex = repository.findMaxOrderIndexByColumn(column);
        return maxOrderIndex == null ? 0 : maxOrderIndex + 1;
    }

    private void moveTaskToPosition(final Task task, final TaskColumn targetColumn, final int targetOrderIndex) {
        final TaskColumn sourceColumn = task.getColumn();
        final List<Task> sourceTasks = repository.findAllByColumnOrderByOrderIndexAsc(sourceColumn);
        sourceTasks.removeIf(it -> it.getId().equals(task.getId()));

        if (!sourceColumn.equals(targetColumn)) {
            reindexTasks(sourceTasks);
            repository.saveAll(sourceTasks);

            final List<Task> targetTasks = repository.findAllByColumnOrderByOrderIndexAsc(targetColumn);
            insertTaskAtPosition(task, targetColumn, targetOrderIndex, targetTasks);
            repository.saveAll(targetTasks);
        } else {
            insertTaskAtPosition(task, targetColumn, targetOrderIndex, sourceTasks);
            repository.saveAll(sourceTasks);
        }
    }

    private void insertTaskAtPosition(Task task, TaskColumn column, int targetOrderIndex, List<Task> tasks) {
        final int normalizedIndex = Math.min(Math.max(targetOrderIndex, 0), tasks.size());
        task.setColumn(column);
        task.setProject(column.getProject());
        tasks.add(normalizedIndex, task);
        reindexTasks(tasks);
    }

    private void reindexColumn(TaskColumn column) {
        final List<Task> tasks = repository.findAllByColumnOrderByOrderIndexAsc(column);
        if (tasks.isEmpty()) {
            return;
        }
        reindexTasks(tasks);
        repository.saveAll(tasks);
    }

    private void reindexTasks(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setOrderIndex(i);
        }
    }

    private UUID resolveExecutorId(String executorId) {
        if (executorId == null || executorId.isBlank()) {
            return null;
        }
        return parseUuid(executorId, TaskValidationConstats.EXECUTOR_ID_INVALID_MESSAGE);
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "orderIndex");
        }

        return switch (sortBy.trim().toLowerCase(Locale.ROOT)) {
            case "title" -> Sort.by(Sort.Direction.ASC, "title");
            case "status" -> Sort.by(Sort.Direction.ASC, "status");
            case "duedate", "due_date" -> Sort.by(Sort.Direction.ASC, "dueDate");
            case "orderindex", "order_index" -> Sort.by(Sort.Direction.ASC, "orderIndex");
            default -> throw new RestApiException(HttpStatus.BAD_REQUEST, TaskValidationConstats.SORT_BY_INVALID_MESSAGE);
        };
    }

    private UUID parseUuid(final String rawId, final String errorMessage) {
        try {
            return UUID.fromString(rawId);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    private void applyDueDate(Task task, LocalDateTime dueDate) {
        task.setDueDate(dueDate);
        task.setDueDateReminder24Sent(false);
        task.setDueDateReminder1Sent(false);
    }
}