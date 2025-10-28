package de.upteams.tasktracker.task.controller;

import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.task.controller.api.TaskApi;
import de.upteams.tasktracker.task.dto.*;
import de.upteams.tasktracker.task.entity.TaskStatus;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class TaskController implements TaskApi {

    /**
     * Service for various operations with Tasks
     */
    private final TaskService service;

    @Override
    public TaskDto save(
            String projectId,
            @Valid TaskCreateRequestDto task,
            AuthUserDetails principal
    ) {
        return service.save(projectId, task, principal.user());
    }

    @Override
    public TaskDto getById(
            String id,
            AuthUserDetails principal
    ) {
        return service.getById(id, principal.user());
    }

    @Override
    public List<TaskDto> getAll(
            String projectId,
            TaskStatus status,
            String executorId,
            LocalDateTime dueBefore,
            String sortBy,
            AuthUserDetails principal
    ) {
        final TaskFilterParams filterParams = new TaskFilterParams(status, executorId, dueBefore, sortBy);
        return service.getAll(projectId, filterParams, principal.user());
    }

    @Override
    public List<TaskDto> getAllByColumn(String columnId, AuthUserDetails principal) {
        return service.getAllByColumn(columnId, principal.user());
    }

    @Override
    public void deleteById(
            String id,
            AuthUserDetails principal
    ) {
        service.delete(id, principal.user());
    }

    @Override
    public TaskDto update(String id, @Valid TaskUpdateRequestDto updateDto, AuthUserDetails principal) {
        return service.updateTask(id, updateDto, principal.user());
    }

    @Override
    public TaskDto move(String id, @Valid TaskMoveRequestDto moveDto, AuthUserDetails principal) {
        return service.moveTask(id, moveDto, principal.user());
    }
}