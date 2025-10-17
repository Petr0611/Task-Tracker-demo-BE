package de.upteams.tasktracker.invitation.controller.api;

import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.invitation.dto.InvitationAcceptResponseDto;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Invitation controller", description = "Endpoints for managing project invitations")
@RequestMapping(value = "/api/v1/invitations", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("isAuthenticated()")
public interface InvitationApi {

    @Operation(summary = "Accept invitation", description = "Activate invitation by invite token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation accepted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InvitationAcceptResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invitation token is invalid or expired",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/accept")
    InvitationAcceptResponseDto acceptInvitation(
            @RequestParam("inviteToken")
            @Parameter(description = "Invitation token received by email", required = true)
            @NotBlank
            String inviteToken,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );
}