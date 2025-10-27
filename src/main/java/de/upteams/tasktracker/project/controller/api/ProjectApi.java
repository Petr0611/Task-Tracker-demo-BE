package de.upteams.tasktracker.project.controller.api;

import de.upteams.tasktracker.collaborator.dto.UpdateCollaboratorRolesDto;
import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.exception.handling.response.ValidationErrorDto;
import de.upteams.tasktracker.invitation.dto.ProjectInvitationResponseDto;
import de.upteams.tasktracker.project.dto.request.ProjectCollaboratorAddRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.request.ProjectInvitationRequestDto;
import de.upteams.tasktracker.project.dto.request.ProjectUpdateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.dto.response.RoleResponse;
import de.upteams.tasktracker.security.service.AuthUserDetails;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Project API description for Swagger
 */
@Tag(name = "Project controller", description = "Controller for various operations with Projects")
@RequestMapping("/api/v1/projects")
@PreAuthorize("isAuthenticated()")
public interface ProjectApi {

    @Operation(summary = "Save/create Project", description = "Save new Project to the Database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "7",
                                      "title": "New Website Development",
                                      "description": "A Project to develop a new company website",
                                      "owner": {
                                        "id": "42",
                                        "email": "tes_dev@upteams.de",
                                        "firstName": "Test",
                                        "lastName": "Dev"
                                      }
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid project payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      { "field": "title", "messages": ["must not be blank"] }
                                    ]
                                    """))
            )
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    ProjectResponseDto save(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Instance of Project to save"
            )
            @Valid
            ProjectCreateDto newProjectDto,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Get Project", description = "Get one Project from the Database by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponseDto.class)))
            ,
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Project not found with id: 7",
                                      "path": "/api/v1/projects/7"
                                    }
                                    """)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("@permissionEvaluator.hasAnyRole(#id, authentication, T(java.util.List).of(T(de.upteams.tasktracker.collaborator.entity.ProjectRoles).VIEWER, T(de.upteams.tasktracker.collaborator.entity.ProjectRoles).ADMIN, T(de.upteams.tasktracker.collaborator.entity.ProjectRoles).OWNER))")
    ProjectResponseDto getById(
            @PathVariable
            @Parameter(required = true, description = "Project ID to search")
            String id
    );

    @Operation(summary = "Get all Projects", description = "Get all Projects from the Database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Projects list",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProjectResponseDto.class))))
    })

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    List<ProjectResponseDto> getAll(@AuthenticationPrincipal AuthUserDetails principal);

//    List<Project> getAll(AuthUserDetails principal);

    @Operation(summary = "Delete Project", description = "Delete Project from the Database by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Project not found with id: 7",
                                      "path": "/api/v1/projects/7"
                                    }
                                    """)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionEvaluator.isOwner(#id, authentication)")
    void deleteById(
            @PathVariable
            @Parameter(required = true, description = "Project ID to delete")
            String id
    );

    @Operation(summary = "Update Project", description = "Update the Project's title and/or description by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class))))
    })
    @PutMapping("/{id}")
    @PreAuthorize("@permissionEvaluator.hasAnyRole(#id, authentication, T(java.util.List).of(T(de.upteams.tasktracker.collaborator.entity.ProjectRoles).OWNER, T(de.upteams.tasktracker.collaborator.entity.ProjectRoles).ADMIN))")
    ProjectResponseDto update(
            @PathVariable
            @Parameter(required = true, description = "Project ID to update")
            String id,

            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Updated Project fields"
            )
            @Valid
            ProjectUpdateDto updateDto
    );

    @Operation(summary = "Add collaborator to project", description = "Assign a user to the project team with specified roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Collaborator successfully added"),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Project or user not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Collaborator already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{id}/collaborators")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionEvaluator.hasAnyRole(#id, authentication, T(java.util.List).of(T(de.upteams.tasktracker.collaborator.entity.ProjectRoles).OWNER, T(de.upteams.tasktracker.collaborator.entity.ProjectRoles).ADMIN))")
    void addUserToProject(
            @PathVariable
            @Parameter(required = true, description = "Project ID to update")
            String id,

            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Collaborator assignment data"
            )
            @Valid
            ProjectCollaboratorAddRequestDto requestDto,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Invite collaborator to project", description = "Send an email invitation to join the project team")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation sent to registered user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectInvitationResponseDto.class))),
            @ApiResponse(responseCode = "202", description = "Invitation sent to unregistered user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectInvitationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/{id}/invitations")
    @PreAuthorize("@permissionEvaluator.hasAnyRole(#id, authentication, T(java.util.List).of(T(de.upteams.tasktracker.collaborator.entity.ProjectRoles).OWNER, T(de.upteams.tasktracker.collaborator.entity.ProjectRoles).ADMIN))")
    ResponseEntity<ProjectInvitationResponseDto> inviteUserToProject(
            @PathVariable
            @Parameter(required = true, description = "Project ID to invite to")
            String id,

            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Invitation payload"
            )
            @Valid
            ProjectInvitationRequestDto requestDto,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(
            summary = "Update collaborator roles",
            description = "Allows OWNER or ADMIN to change roles of an existing project collaborator."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Roles updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden — only OWNER or ADMIN can update roles",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Project or collaborator not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/{projectId}/collaborators/{userId}/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateCollaboratorRoles(
            @PathVariable String projectId,
            @PathVariable String userId,
            @RequestBody @Valid UpdateCollaboratorRolesDto dto,
            @AuthenticationPrincipal @Parameter(hidden = true) AuthUserDetails principal
    );

    @Operation(
            summary = "Get current user's role in project",
            description = "Returns the role (OWNER, ADMIN, MEMBER, or VIEWER) of the authenticated user for the given project"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Role retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RoleResponse.class),
                    examples = @ExampleObject(value = """
                            { "role": "ADMIN" }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden — user does not have access to the project",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    )
    @GetMapping("/{projectId}/role")
    RoleResponse getUserRole(
            @PathVariable
            @Parameter(description = "Project ID to check user's role for")
            String projectId,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Get current user's projects", description = "Returns only the projects owned by the current authenticated user")
    @ApiResponse(responseCode = "200", description = "List of user's projects",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ProjectResponseDto.class))))
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    List<ProjectResponseDto> getMyProjects(
            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );


}
