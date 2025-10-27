package de.upteams.tasktracker.taskcomment.controller.api;

import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.exception.handling.response.ValidationErrorDto;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import de.upteams.tasktracker.taskcomment.dto.CommentCreateRequestDto;
import de.upteams.tasktracker.taskcomment.dto.CommentDto;
import de.upteams.tasktracker.taskcomment.dto.CommentUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Task comments controller")
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/tasks/{taskId}/comments")
public interface TaskCommentApi {

    @Operation(summary = "Get task comments", description = "Returns all comments for the given task ordered by creation time")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of comments returned",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CommentDto.class)))),
            @ApiResponse(responseCode = "403", description = "User has no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping
    List<CommentDto> getComments(
            @PathVariable
            @Parameter(description = "Identifier of the task", schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN))
            String taskId,
            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Create comment", description = "Creates a new comment in the specified task")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)))),
            @ApiResponse(responseCode = "403", description = "User has no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CommentDto addComment(
            @PathVariable
            @Parameter(description = "Identifier of the task", schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN))
            String taskId,
            @RequestBody @Valid CommentCreateRequestDto requestDto,
            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Get comment", description = "Returns a single comment by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{commentId}")
    CommentDto getComment(
            @PathVariable
            @Parameter(description = "Identifier of the task", schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN))
            String taskId,
            @PathVariable
            @Parameter(description = "Identifier of the comment", schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN))
            String commentId,
            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Update comment", description = "Updates comment text")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/{commentId}")
    CommentDto updateComment(
            @PathVariable
            @Parameter(description = "Identifier of the task", schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN))
            String taskId,
            @PathVariable
            @Parameter(description = "Identifier of the comment", schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN))
            String commentId,
            @RequestBody @Valid CommentUpdateRequestDto requestDto,
            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Delete comment", description = "Deletes the specified comment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comment deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteComment(
            @PathVariable
            @Parameter(description = "Identifier of the task", schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN))
            String taskId,
            @PathVariable
            @Parameter(description = "Identifier of the comment", schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN))
            String commentId,
            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );
}