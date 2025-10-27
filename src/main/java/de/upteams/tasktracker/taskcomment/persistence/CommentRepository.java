package de.upteams.tasktracker.taskcomment.persistence;

import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.taskcomment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findAllByTaskOrderByCreatedAtAsc(Task task);

    Optional<Comment> findByIdAndTask(UUID commentId, Task task);
}