package de.upteams.tasktracker.taskcomment.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

public class CommentNotFoundException extends RestApiException {

    public CommentNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Comment not found");
    }
}