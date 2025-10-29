package de.upteams.tasktracker.invitation.controller;

import de.upteams.tasktracker.invitation.controller.api.InvitationApi;
import de.upteams.tasktracker.invitation.dto.InvitationAcceptResponseDto;
import de.upteams.tasktracker.invitation.service.interfaces.InvitationService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InvitationController implements InvitationApi {

    private final InvitationService invitationService;

    @Override
    public InvitationAcceptResponseDto acceptInvitation(
            String inviteToken,
            @AuthenticationPrincipal
            AuthUserDetails principal
    ) {
        InvitationService.InvitationAcceptanceResult result =
                invitationService.acceptInvitation(inviteToken, principal != null ? principal.user() : null);

        return new InvitationAcceptResponseDto(
                result.invitation().getProject().getId().toString(),
                result.invitation().getProject().getTitle(),
                result.collaborator().getStatus(),
                result.invitation().getRole()
        );
    }
}
