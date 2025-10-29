package de.upteams.tasktracker.taskcolumn.dto;

import de.upteams.tasktracker.task.dto.TaskDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO that represents a single column on the board together with its Tasks.
 */
@Value
@Schema(description = "Data Transfer Object for Task column")
public class TaskColumnDto {

    @Schema(
            description = "Unique identifier of the column",
            example = "a7d1b4f3-7c37-4c1c-921d-44ebd9b4797d",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    String id;

    @Schema(description = "Title of the column", example = "In Progress")
    String title;

    @Schema(description = "Order in which the column should be displayed", example = "1")
    Integer orderIndex;

    @Schema(
            description = "Identifier of the project this column belongs to",
            example = "c7243236-8537-4421-bbe0-2744e37032e3"
    )
    String projectId;

    @Schema(description = "Indicates whether the column is protected from deletion", example = "true")
    boolean baseColumn;


    @Schema(
            description = "Tasks that belong to this column",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    List<TaskDto> tasks = new ArrayList<>();
}