package de.upteams.tasktracker.taskcolumn.template.entity;

import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;

@Entity
@Table(name = "task_column_template_column")
@Getter
@Setter
@NoArgsConstructor
public class TaskColumnTemplateColumn extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private TaskColumnTemplate template;

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

    @Override
    public String toString() {
        return "TaskColumnTemplateColumn{" +
                "id=" + id +
                ", templateId=" + getIdForToString(template) +
                ", title='" + title + '\'' +
                ", orderIndex=" + orderIndex +
                '}';
    }
}