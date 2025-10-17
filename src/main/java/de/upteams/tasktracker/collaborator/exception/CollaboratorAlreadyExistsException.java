package de.upteams.tasktracker.collaborator.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when trying to add a collaborator that is already assigned to the project.
 */
public class CollaboratorAlreadyExistsException extends RestApiException {

    private static final String MESSAGE = "Collaborator is already assigned to the project";

    public CollaboratorAlreadyExistsException() {
        super(HttpStatus.CONFLICT, MESSAGE);
    }
}
