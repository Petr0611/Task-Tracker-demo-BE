package de.upteams.tasktracker.taskcomment.service.impl;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.mail.EmailService;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import de.upteams.tasktracker.taskcomment.dto.CommentCreateRequestDto;
import de.upteams.tasktracker.taskcomment.dto.CommentDto;
import de.upteams.tasktracker.taskcomment.dto.CommentUpdateRequestDto;
import de.upteams.tasktracker.taskcomment.entity.Comment;
import de.upteams.tasktracker.taskcomment.exception.CommentNotFoundException;
import de.upteams.tasktracker.taskcomment.persistence.CommentRepository;
import de.upteams.tasktracker.taskcomment.service.TaskCommentService;
import de.upteams.tasktracker.taskcomment.utils.CommentMappingService;
import de.upteams.tasktracker.taskcomment.utils.CommentValidationConstants;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskCommentServiceImpl implements TaskCommentService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9._%+-]+(?:@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})?)");

    private static final String COMMENT_MANAGE_FORBIDDEN_MESSAGE = "Only comment author or project owner can modify comments";

    private final CommentRepository commentRepository;
    private final TaskService taskService;
    private final CollaboratorService collaboratorService;
    private final CommentMappingService mappingService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Override
    public List<CommentDto> getComments(String taskId, AppUser requester) {
        Task task = getTaskWithAccessCheck(taskId, requester);
        return commentRepository.findAllByTaskOrderByCreatedAtAsc(task)
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public CommentDto getComment(String taskId, String commentId, AppUser requester) {
        Task task = getTaskWithAccessCheck(taskId, requester);
        Comment comment = findCommentForTask(commentId, task);
        return mappingService.mapEntityToDto(comment);
    }

    @Override
    @Transactional
    public CommentDto addComment(String taskId, CommentCreateRequestDto requestDto, AppUser author) {
        Task task = getTaskWithAccessCheck(taskId, author);
        Comment comment = new Comment(author, task, requestDto.text().trim());
        Comment saved = commentRepository.save(comment);
        notifyMentionedUsers(saved);
        return mappingService.mapEntityToDto(saved);
    }

    @Override
    @Transactional
    public CommentDto updateComment(String taskId, String commentId, CommentUpdateRequestDto requestDto, AppUser editor) {
        Task task = getTaskWithAccessCheck(taskId, editor);
        Comment comment = findCommentForTask(commentId, task);
        ensureCommentManagementPermission(task, comment, editor);
        comment.setText(requestDto.text().trim());
        Comment updated = commentRepository.save(comment);
        notifyMentionedUsers(updated);
        return mappingService.mapEntityToDto(updated);
    }

    @Override
    @Transactional
    public void deleteComment(String taskId, String commentId, AppUser requester) {
        Task task = getTaskWithAccessCheck(taskId, requester);
        Comment comment = findCommentForTask(commentId, task);
        ensureCommentManagementPermission(task, comment, requester);
        commentRepository.delete(comment);
    }

    private Comment findCommentForTask(String commentId, Task task) {
        UUID id = parseUuid(commentId, CommentValidationConstants.COMMENT_ID_INVALID_MESSAGE);
        return commentRepository.findByIdAndTask(id, task).orElseThrow(CommentNotFoundException::new);
    }

    private Task getTaskWithAccessCheck(String taskId, AppUser user) {
        Task task = taskService.getOrThrow(taskId);
        enforceProjectAccess(task.getProject(), user);
        return task;
    }

    private void enforceProjectAccess(Project project, AppUser user) {
        if (isProjectOwner(project, user)) {
            return;
        }
        boolean participant = collaboratorService.isUserInProject(user, project);
        if (!participant) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }
    }

    private boolean isProjectOwner(Project project, AppUser user) {
        return project.getOwner() != null && project.getOwner().equals(user);
    }

    private void ensureCommentManagementPermission(Task task, Comment comment, AppUser user) {
        if (comment.getAuthor().equals(user) || isProjectOwner(task.getProject(), user)) {
            return;
        }
        boolean hasAdminRole = collaboratorService.hasUserPermission(
                user,
                task.getProject(),
                List.of(ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        if (!hasAdminRole) {
            throw new RestApiException(HttpStatus.FORBIDDEN, COMMENT_MANAGE_FORBIDDEN_MESSAGE);
        }
    }

    private UUID parseUuid(String id, String errorMessage) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    private void notifyMentionedUsers(Comment comment) {
        Set<AppUser> mentionedUsers = extractMentionedUsers(comment.getText());
        if (mentionedUsers.isEmpty()) {
            return;
        }
        mentionedUsers.stream()
                .filter(user -> !user.equals(comment.getAuthor()))
                .forEach(user -> emailService.sendCommentMentionNotification(
                        user.getEmail(),
                        comment.getTask().getProject().getTitle(),
                        comment.getTask().getTitle(),
                        Optional.ofNullable(comment.getAuthor().getDisplayName()).orElse(comment.getAuthor().getEmail()),
                        comment.getText(),
                        comment.getTask().getProject().getId().toString(),
                        comment.getTask().getId().toString()
                ));
    }

    private Set<AppUser> extractMentionedUsers(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        Matcher matcher = MENTION_PATTERN.matcher(text);
        Set<AppUser> users = new HashSet<>();
        while (matcher.find()) {
            String mention = matcher.group(1);
            if (mention == null || mention.isBlank()) {
                continue;
            }
            resolveMention(mention).ifPresent(users::add);
        }
        return users;
    }

    private Optional<AppUser> resolveMention(String mention) {
        if (mention.contains("@")) {
            return userRepository.findByEmailIgnoreCase(mention);
        }
        Optional<AppUser> byDisplayName = userRepository.findByDisplayNameIgnoreCase(mention);
        if (byDisplayName.isPresent()) {
            return byDisplayName;
        }
        return userRepository.findByEmailLocalPartIgnoreCase(mention);
    }
}