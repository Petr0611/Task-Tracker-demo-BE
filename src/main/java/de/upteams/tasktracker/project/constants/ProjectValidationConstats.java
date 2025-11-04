package de.upteams.tasktracker.project.constants;

public final class ProjectValidationConstats {

    private ProjectValidationConstats() {
        throw new IllegalStateException("Utility class");
    }

    public static final String BASE_REGEX = "^[A-Z][a-zA-Z0-9,.%:?&!$;*() \\-]+$";
    public static final String BASE_MESSAGE =
            "Must start with a capital English letter (A–Z) and contain only Latin characters, digits, spaces, and punctuation marks.";

    public static final int TITLE_MIN = 3;
    public static final int TITLE_MAX = 155;

    public static final int DESCRIPTION_MIN = 3;
    public static final int DESCRIPTION_MAX = 500;
}
