package de.upteams.tasktracker.exception.handling.exceptions.common;

public class OwnerAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "Project already has an OWNER. Only one OWNER is allowed per project.";

    public OwnerAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public OwnerAlreadyExistsException(String message) {
        super(message);
    }
}
