package de.upteams.tasktracker.task.constants;

public final class TaskValidationConstats {

    public static final String UUID_PATTERN =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    public static final String TASK_ID_INVALID_MESSAGE = "Task ID must be a valid UUID";

    public static final String PROJECT_ID_INVALID_MESSAGE = "Project ID must be a valid UUID";

    public static final String COLUMN_ID_INVALID_MESSAGE = "Column ID must be a valid UUID";

    public static final String ORDER_INDEX_INVALID_MESSAGE = "Order index must be zero or positive";

    public static final String DUE_DATE_INVALID_MESSAGE = "Due date must be in the future";

    private TaskValidationConstats() {
        throw new IllegalStateException("Utility class");
    }
}
