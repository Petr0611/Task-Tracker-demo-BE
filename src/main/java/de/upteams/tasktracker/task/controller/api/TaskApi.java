package de.upteams.tasktracker.task.controller.api;

import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.exception.handling.response.ValidationErrorDto;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import de.upteams.tasktracker.task.dto.TaskCreateRequestDto;
import de.upteams.tasktracker.task.dto.TaskDto;
import de.upteams.tasktracker.task.dto.TaskUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Tag(name = "Task controller")
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/tasks")
public interface TaskApi {

    @Operation(summary = "Create/save Task", description = "Creates a new task associated with a project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "5",
                                      "title": "Implement repository layer",
                                      "description": "Create JPA repositories for all entities",
                                      "project": { "id": "7", "title": "New Website Development" },
                                      "executors": []
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid task payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      { "field": "title", "messages": ["must not be blank"] }
                                    ]
                                    """))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - user has no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "User has no access to this project",
                                      "path": "/api/v1/tasks"
                                    }
                                    """))
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TaskDto save(
            @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Payload for creating a new Task"
            )
            @Valid
            TaskCreateRequestDto task,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Get Task", description = "Retrieves a task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskDto.class)))
            ,
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Task not found with id: 5",
                                      "path": "/api/v1/tasks/5"
                                    }
                                    """))
            )
    })
    @GetMapping("/{id}")
    TaskDto getById(
            @PathVariable
            @Parameter(
                    description = "Unique identifier of the Task",
                    required = true,
                    schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN)
            )
            String id,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Get all Tasks for Project", description = "Retrieves all tasks under a specific project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of tasks",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TaskDto.class))))
            ,
            @ApiResponse(responseCode = "403", description = "Forbidden - user has no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @GetMapping("/project/{projectId}")
    List<TaskDto> getAll(
            @PathVariable
            @Parameter(
                    description = "Unique identifier of the Project",
                    required = true,
                    schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN)
            )
            String projectId,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Delete Task", description = "Deletes a task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
            ,
            @ApiResponse(responseCode = "403", description = "Forbidden - user has no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteById(
            @PathVariable
            @Parameter(
                    description = "Unique identifier of the Task",
                    required = true,
                    schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN)
            )
            String id,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Update Task", description = "Updates an existing task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)))),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })

    @PutMapping("/{id}")
    TaskDto update(
            @PathVariable
            @Parameter(
                    description = "Unique identifier of the Task",
                    required = true,
                    schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN)
            )
            String id,

            @org.springframework.web.bind.annotation.RequestBody
            @Parameter(description = "Updated task data", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Partial Task data to update"
            )
            @Valid
            TaskUpdateRequestDto updateDto,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );
}