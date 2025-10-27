package de.upteams.tasktracker.taskcomment.controller;

import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.taskcomment.controller.api.TaskCommentApi;
import de.upteams.tasktracker.taskcomment.dto.CommentCreateRequestDto;
import de.upteams.tasktracker.taskcomment.dto.CommentDto;
import de.upteams.tasktracker.taskcomment.dto.CommentUpdateRequestDto;
import de.upteams.tasktracker.taskcomment.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class TaskCommentController implements TaskCommentApi {

    private final TaskCommentService commentService;

    @Override
    public List<CommentDto> getComments(String taskId, AuthUserDetails principal) {
        return commentService.getComments(taskId, principal.user());
    }

    @Override
    public CommentDto addComment(String taskId, CommentCreateRequestDto requestDto, AuthUserDetails principal) {
        return commentService.addComment(taskId, requestDto, principal.user());
    }

    @Override
    public CommentDto getComment(String taskId, String commentId, AuthUserDetails principal) {
        return commentService.getComment(taskId, commentId, principal.user());
    }

    @Override
    public CommentDto updateComment(String taskId, String commentId, CommentUpdateRequestDto requestDto, AuthUserDetails principal) {
        return commentService.updateComment(taskId, commentId, requestDto, principal.user());
    }

    @Override
    public void deleteComment(String taskId, String commentId, AuthUserDetails principal) {
        commentService.deleteComment(taskId, commentId, principal.user());
    }
}