package de.upteams.tasktracker.taskcolumn.entity;

import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.validator.constraints.Length;

import java.util.LinkedHashSet;
import java.util.Set;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;
import static de.upteams.tasktracker.utils.EntityUtil.getIdsForToString;

/**
 * Column entity for organizing Tasks within a Project.
 */
@Entity
@Table(name = "task_column")
@Getter
@Setter
@NoArgsConstructor
public class TaskColumn extends BaseEntity {

    @NotBlank
    @Length(min = 3, max = 255)
    @Pattern(
            regexp = "[A-Z][a-zA-Z1-9 ]{2,}",
            message = "Column title should be at least 3 character length and start with capital letter"
    )
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Min(0)
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "base_column", nullable = false)
    @ColumnDefault("false")
    private boolean baseColumn;

    @OneToMany(mappedBy = "column", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<Task> tasks = new LinkedHashSet<>();

    public TaskColumn(String title, Integer orderIndex, Project project) {
        this.title = title;
        this.orderIndex = orderIndex;
        this.project = project;
    }

    @Override
    public String toString() {
        return "TaskColumn{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", orderIndex=" + orderIndex +
                ", projectId=" + getIdForToString(project) +
                ", baseColumn=" + baseColumn +
                ", tasksIds=" + getIdsForToString(tasks) +
                '}';
    }
}