package de.upteams.tasktracker.taskcolumn.controller;

import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.taskcolumn.controller.api.TaskColumnApi;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnCreateRequestDto;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnDto;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnUpdateRequestDto;
import de.upteams.tasktracker.taskcolumn.service.interfaces.TaskColumnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class TaskColumnController implements TaskColumnApi {

    private final TaskColumnService service;

    @Override
    public TaskColumnDto createColumn(String projectId, @Valid TaskColumnCreateRequestDto requestDto, AuthUserDetails principal) {
        return service.create(projectId, requestDto, principal.user());
    }

    @Override
    public List<TaskColumnDto> getColumns(String projectId, AuthUserDetails principal) {
        return service.getAll(projectId, principal.user());
    }

    @Override
    public TaskColumnDto getColumn(String columnId, AuthUserDetails principal) {
        return service.getById(columnId, principal.user());
    }

    @Override
    public TaskColumnDto updateColumn(String columnId, @Valid TaskColumnUpdateRequestDto requestDto, AuthUserDetails principal) {
        return service.update(columnId, requestDto, principal.user());
    }

    @Override
    public void deleteColumn(String columnId, AuthUserDetails principal) {
        service.delete(columnId, principal.user());
    }
}