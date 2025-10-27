package de.upteams.tasktracker.taskcomment.utils;

public final class CommentValidationConstants {

    public static final String COMMENT_ID_INVALID_MESSAGE = "Comment ID must be a valid UUID";

    private CommentValidationConstants() {
        throw new IllegalStateException("Utility class");
    }
}