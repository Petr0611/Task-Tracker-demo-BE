package de.upteams.tasktracker.exception.handling.exceptions.common;

public class OwnerAlreadyExistsException extends RuntimeException {

    public OwnerAlreadyExistsException(String message) {
        super(message);
    }
}
