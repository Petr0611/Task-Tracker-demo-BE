package de.upteams.tasktracker.security.exception;

public class UnexpectedPrincipalTypeException extends RuntimeException {

    public UnexpectedPrincipalTypeException(Object principal) {
        super("Unexpected principal type: " + (principal != null ? principal.getClass().getName() : "null"));
    }
}
