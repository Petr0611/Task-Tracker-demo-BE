package de.upteams.tasktracker.taskcolumn.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested Task column does not exist.
 */
public class TaskColumnNotFoundException extends RestApiException {

    public TaskColumnNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Task column not found");
    }
}