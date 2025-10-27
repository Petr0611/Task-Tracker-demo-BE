package de.upteams.tasktracker.taskcomment.service;

import de.upteams.tasktracker.taskcomment.dto.CommentCreateRequestDto;
import de.upteams.tasktracker.taskcomment.dto.CommentDto;
import de.upteams.tasktracker.taskcomment.dto.CommentUpdateRequestDto;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;

public interface TaskCommentService {

    List<CommentDto> getComments(String taskId, AppUser requester);

    CommentDto getComment(String taskId, String commentId, AppUser requester);

    CommentDto addComment(String taskId, CommentCreateRequestDto requestDto, AppUser author);

    CommentDto updateComment(String taskId, String commentId, CommentUpdateRequestDto requestDto, AppUser editor);

    void deleteComment(String taskId, String commentId, AppUser requester);
}