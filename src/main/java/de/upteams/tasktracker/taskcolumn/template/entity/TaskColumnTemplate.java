package de.upteams.tasktracker.taskcolumn.template.entity;

import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;

@Entity
@Table(name = "task_column_template")
@Getter
@Setter
@NoArgsConstructor
public class TaskColumnTemplate extends BaseEntity {

    @NotBlank
    @Length(min = 3, max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @OneToMany(
            mappedBy = "template",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("orderIndex ASC")
    private final List<TaskColumnTemplateColumn> columns = new ArrayList<>();

    public void addColumn(TaskColumnTemplateColumn column) {
        columns.add(column);
        column.setTemplate(this);
    }

    @Override
    public String toString() {
        return "TaskColumnTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ownerId=" + getIdForToString(owner) +
                ", columnsCount=" + columns.size() +
                '}';
    }
}