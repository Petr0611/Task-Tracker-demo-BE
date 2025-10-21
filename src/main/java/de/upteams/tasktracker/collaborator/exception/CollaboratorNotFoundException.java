package de.upteams.tasktracker.collaborator.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

public class CollaboratorNotFoundException extends RestApiException {

    private static final String MESSAGE = "Collaborator not found";

    public CollaboratorNotFoundException() {
        super(HttpStatus.NOT_FOUND, MESSAGE);

    }
}
