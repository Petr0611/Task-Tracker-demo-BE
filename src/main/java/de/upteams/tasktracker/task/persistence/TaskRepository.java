package de.upteams.tasktracker.task.persistence;

import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.taskcolumn.entity.TaskColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {
    @Query("select t from Task t where t.project = ?1")
    List<Task> findByProject(Project project);

    @Query("select t from Task t where t.column = ?1")
    List<Task> findByColumn(TaskColumn column);

    List<Task> findAllByColumnOrderByOrderIndexAsc(TaskColumn column);

    @Query("select coalesce(max(t.orderIndex), -1) from Task t where t.column = ?1")
    Integer findMaxOrderIndexByColumn(TaskColumn column);

    @Query("""
            select distinct t from Task t
            left join fetch t.executors exec
            left join fetch exec.appUser
            left join fetch t.project project
            left join fetch project.owner
            where t.dueDate is not null
              and t.dueDate between :now and :deadline
              and t.dueDateReminder24Sent = false
            """)
    List<Task> findTasksFor24HourReminder(@Param("now") LocalDateTime now, @Param("deadline") LocalDateTime deadline);

    @Query("""
            select distinct t from Task t
            left join fetch t.executors exec
            left join fetch exec.appUser
            left join fetch t.project project
            left join fetch project.owner
            where t.dueDate is not null
              and t.dueDate between :now and :deadline
              and t.dueDateReminder1Sent = false
            """)
    List<Task> findTasksForOneHourReminder(@Param("now") LocalDateTime now, @Param("deadline") LocalDateTime deadline);
}
