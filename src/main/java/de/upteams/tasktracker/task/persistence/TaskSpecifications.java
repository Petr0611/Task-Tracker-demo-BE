package de.upteams.tasktracker.task.persistence;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.entity.TaskStatus;
import de.upteams.tasktracker.user.entity.AppUser;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Specification helpers for querying {@link Task} entities.
 */
public final class TaskSpecifications {

    private TaskSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Task> belongsToProject(Project project) {
        return (root, query, builder) -> builder.equal(root.get("project"), project);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<Task> hasExecutor(UUID executorId) {
        return (root, query, builder) -> {
            query.distinct(true);
            final Join<Task, Collaborator> executors = root.join("executors", JoinType.LEFT);
            final Join<Collaborator, AppUser> users = executors.join("appUser", JoinType.LEFT);
            return builder.equal(users.get("id"), executorId);
        };
    }

    public static Specification<Task> dueBefore(LocalDateTime dueBefore) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("dueDate"), dueBefore);
    }
}