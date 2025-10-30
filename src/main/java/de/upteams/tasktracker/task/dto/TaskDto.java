package de.upteams.tasktracker.task.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import de.upteams.tasktracker.task.entity.TaskStatus;
import de.upteams.tasktracker.user.dto.EmployeeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Task DTO
 */
@Schema(description = "Data Transfer Object for Task entity")
@Value
public class TaskDto {

    @Schema(
            description = "Unique identifier of the Task",
            example = "5",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    String id;

    @Schema(description = "Title of the Task", example = "Implement repository layer")
    String title;

    @Schema(
            description = "Detailed description of the Task",
            example = "Create JPA repositories for all entities"
    )
    String description;

    @Schema(
            description = "Identifier of the column this Task belongs to",
            example = "5b70c020-07a5-43c5-b4db-5bd4f72bae94"
    )
    String columnId;

    @Schema(
            description = "Title of the column this Task belongs to",
            example = "In Progress",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    String columnTitle;

    @Schema(
            description = "Current status of the Task",
            example = "IN_PROGRESS"
    )
    TaskStatus status;

    @Schema(
            description = "Order index of the task inside its column",
            example = "3",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    Integer orderIndex;

    @Schema(
            description = "Deadline of the task",
            example = "2024-05-01T18:00:00"
    )
    LocalDateTime dueDate;

    @JsonIgnore
    @Schema(
            description = "The Project whit which this Task is associated",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    ProjectResponseDto project;

    @Schema(
            description = "Identifier of the Project this Task belongs to",
            example = "c7243236-8537-4421-bbe0-2744e37032e3"
    )
    String projectId;

    @Schema(
            description = "List of attachments linked to this task",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    List<AttachmentDto> attachments = new ArrayList<>();

    @Schema(
            description = "List of Users assigned to this Task",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    Set<EmployeeDto> executors = new HashSet<>();

}
