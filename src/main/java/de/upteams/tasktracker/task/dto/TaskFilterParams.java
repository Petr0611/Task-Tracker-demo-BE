package de.upteams.tasktracker.task.dto;

import de.upteams.tasktracker.task.entity.TaskStatus;

import java.time.LocalDateTime;

/**
 * Request parameters for filtering and sorting tasks.
 *
 * @param status     optional task status filter
 * @param executorId optional executor identifier filter
 * @param dueBefore  optional due date upper bound filter
 * @param sortBy     optional sort field parameter
 */
public record TaskFilterParams(
        TaskStatus status,
        String executorId,
        LocalDateTime dueBefore,
        String sortBy
) {
}