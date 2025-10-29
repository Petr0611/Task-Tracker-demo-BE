package de.upteams.tasktracker.taskcolumn.template.controller.api;

import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.exception.handling.response.ValidationErrorDto;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnDto;
import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateApplyRequestDto;
import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateCreateRequestDto;
import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateDto;
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

@Tag(name = "Task column templates")
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/task-columns/templates")
public interface TaskColumnTemplateApi {

    @Operation(summary = "Create template from project", description = "Stores the current column configuration of the provided project as a reusable template")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskColumnTemplateDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/project/{projectId}")
    @ResponseStatus(HttpStatus.CREATED)
    TaskColumnTemplateDto create(
            @PathVariable
            @Parameter(
                    description = "Identifier of the project whose columns should be stored",
                    required = true,
                    schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN)
            )
            String projectId,

            @RequestBody
            @Valid
            TaskColumnTemplateCreateRequestDto requestDto,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "List templates", description = "Returns all templates owned by the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Collection of templates",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TaskColumnTemplateDto.class))))
    })
    @GetMapping
    List<TaskColumnTemplateDto> getAll(
            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Apply template", description = "Replaces the column configuration of a project with the columns defined in the template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template applied successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TaskColumnDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Template not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{templateId}/apply")
    List<TaskColumnDto> apply(
            @PathVariable
            @Parameter(
                    description = "Identifier of the template to apply",
                    required = true,
                    schema = @Schema(pattern = TaskValidationConstats.UUID_PATTERN)
            )
            String templateId,

            @RequestBody
            @Valid
            TaskColumnTemplateApplyRequestDto requestDto,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );
}