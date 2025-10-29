package de.upteams.tasktracker.taskcolumn.template.persistence;

import de.upteams.tasktracker.taskcolumn.template.entity.TaskColumnTemplate;
import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskColumnTemplateRepository extends JpaRepository<TaskColumnTemplate, UUID> {

    @EntityGraph(attributePaths = {"columns"})
    List<TaskColumnTemplate> findAllByOwnerOrderByNameAsc(AppUser owner);

    @EntityGraph(attributePaths = {"columns"})
    Optional<TaskColumnTemplate> findByIdAndOwner(UUID id, AppUser owner);
}