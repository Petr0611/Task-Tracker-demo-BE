package de.upteams.tasktracker.taskcolumn.persistence;

import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.taskcolumn.entity.TaskColumn;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for accessing Task column entities.
 */
@Repository
public interface TaskColumnRepository extends JpaRepository<TaskColumn, UUID> {

    @EntityGraph(attributePaths = {"tasks"})
    List<TaskColumn> findAllByProjectOrderByOrderIndexAsc(Project project);

    Optional<TaskColumn> findByIdAndProject(UUID id, Project project);

    @Query("select coalesce(max(c.orderIndex), -1) from TaskColumn c where c.project = :project")
    Integer findMaxOrderIndex(@Param("project") Project project);
}