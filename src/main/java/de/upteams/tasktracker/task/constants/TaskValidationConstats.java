package de.upteams.tasktracker.task.constants;

public final class TaskValidationConstats {

    public static final String UUID_PATTERN =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    public static final String TASK_ID_INVALID_MESSAGE = "Task ID must be a valid UUID";

    public static final String PROJECT_ID_INVALID_MESSAGE = "Project ID must be a valid UUID";

    private TaskValidationConstats() {
        throw new IllegalStateException("Utility class");
    }
}
