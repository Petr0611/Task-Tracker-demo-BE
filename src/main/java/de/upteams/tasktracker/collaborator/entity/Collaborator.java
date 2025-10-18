package de.upteams.tasktracker.collaborator.entity;


import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;
import static de.upteams.tasktracker.utils.EntityUtil.getIdsForToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Collaborator extends BaseEntity {

    @NotNull
    @ManyToOne
    private AppUser appUser;

    @NotNull
    @ManyToOne
    private Project project;


    @ElementCollection(targetClass = ProjectRoles.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "collaborator_roles",
            joinColumns = @JoinColumn(name = "collaborator_id")
    )
    @Enumerated(EnumType.STRING)
    private Set<ProjectRoles> projectRolesSet = EnumSet.noneOf(ProjectRoles.class);

    @ManyToMany
    private final Set<Task> tasks = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @ColumnDefault("'ACTIVE'")
    private CollaboratorStatus status = CollaboratorStatus.ACTIVE;

    @Override
    public String toString() {
        return "Collaborator{" +
                "id=" + id +
                ", tasks=" + getIdsForToString(tasks) +
                ", projectRolesSet=" + projectRolesSet +
                ", status=" + status +
                ", project=" + getIdForToString(project) +
                ", appUserId=" + getIdForToString(appUser) +
                '}';
    }
}
