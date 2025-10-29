package de.upteams.tasktracker.task.service.interfaces;

import de.upteams.tasktracker.task.dto.TaskCreateRequestDto;
import de.upteams.tasktracker.task.dto.TaskDto;
import de.upteams.tasktracker.task.dto.TaskMoveRequestDto;
import de.upteams.tasktracker.task.dto.TaskUpdateRequestDto;
import de.upteams.tasktracker.task.entity.Attachment;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;
import java.util.Optional;

/**
 * Service for various operations with Tasks
 */
public interface TaskService {

    TaskDto save(String projectId, TaskCreateRequestDto newTaskDto, AppUser creator);

    TaskDto getById(String id, AppUser requester);

    Task getOrThrow(String id);

    Optional<Task> findById(String id);

    List<TaskDto> getAll(String projectId, AppUser authUser);

    List<TaskDto> getAllByColumn(String columnId, AppUser authUser);

    void delete(String id, AppUser changer);

    TaskDto updateTask(String id, TaskUpdateRequestDto updateDto, AppUser changer);

    TaskDto moveTask(String id, TaskMoveRequestDto moveDto, AppUser changer);

    TaskDto addAttachment(String taskId, Attachment attachment, AppUser requester);

    void deleteAttachmentFromTask(String taskId, String attachmentId);

}