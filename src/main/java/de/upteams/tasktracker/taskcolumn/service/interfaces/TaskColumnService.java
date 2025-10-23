package de.upteams.tasktracker.taskcolumn.service.interfaces;

import de.upteams.tasktracker.taskcolumn.dto.TaskColumnCreateRequestDto;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnDto;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnUpdateRequestDto;
import de.upteams.tasktracker.taskcolumn.entity.TaskColumn;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Task columns.
 */
public interface TaskColumnService {

    TaskColumnDto create(String projectId, TaskColumnCreateRequestDto requestDto, AppUser creator);

    TaskColumnDto getById(String id, AppUser requester);

    List<TaskColumnDto> getAll(String projectId, AppUser authUser);

    TaskColumnDto update(String id, TaskColumnUpdateRequestDto requestDto, AppUser changer);

    void delete(String id, AppUser changer);

    TaskColumn getOrThrow(String id);

    Optional<TaskColumn> findById(String id);
}