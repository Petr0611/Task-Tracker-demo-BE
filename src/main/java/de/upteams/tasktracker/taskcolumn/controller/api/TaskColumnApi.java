package de.upteams.tasktracker.taskcolumn.controller.api;

import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.exception.handling.response.ValidationErrorDto;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnCreateRequestDto;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnDto;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnUpdateRequestDto;
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

import java.util.List;

@Tag(name = "Task column controller")
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1")
public interface TaskColumnApi {

    @Operation(summary = "Create a new column", description = "Creates a new column for the specified project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Column successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskColumnDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)))),
            @ApiResponse(responseCode = "403", description = "User has no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/projects/{projectId}/columns")
    @ResponseStatus(HttpStatus.CREATED)
    TaskColumnDto createColumn(
            @PathVariable
            @Parameter(
                    description = "Identifier of the project",
                    required = true,
                    schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN)
            )
            String projectId,

            @RequestBody
            @Valid
            TaskColumnCreateRequestDto requestDto,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Get project columns", description = "Retrieves all columns together with their tasks for the specified project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Columns returned",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TaskColumnDto.class)))),
            @ApiResponse(responseCode = "403", description = "User has no access to the project",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/projects/{projectId}/columns")
    List<TaskColumnDto> getColumns(
            @PathVariable
            @Parameter(
                    description = "Identifier of the project",
                    required = true,
                    schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN)
            )
            String projectId,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Get column", description = "Retrieves column details with tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Column returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskColumnDto.class))),
            @ApiResponse(responseCode = "404", description = "Column not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/columns/{columnId}")
    TaskColumnDto getColumn(
            @PathVariable
            @Parameter(
                    description = "Identifier of the column",
                    required = true,
                    schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN)
            )
            String columnId,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Update column", description = "Updates column title or order index")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Column updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskColumnDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)))),
            @ApiResponse(responseCode = "404", description = "Column not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/columns/{columnId}")
    TaskColumnDto updateColumn(
            @PathVariable
            @Parameter(
                    description = "Identifier of the column",
                    required = true,
                    schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN)
            )
            String columnId,

            @RequestBody
            @Valid
            TaskColumnUpdateRequestDto requestDto,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(
            summary = "Delete column",
            description = "Deletes a column with all associated tasks. Only project owners can remove columns"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Column deleted"),
            @ApiResponse(responseCode = "400", description = "Column cannot be deleted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Only project owners are allowed to delete columns",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Column not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/columns/{columnId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteColumn(
            @PathVariable
            @Parameter(
                    description = "Identifier of the column",
                    required = true,
                    schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN)
            )
            String columnId,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );
}