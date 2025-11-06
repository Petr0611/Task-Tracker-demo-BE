package de.upteams.tasktracker.invitation.service.interfaces;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.invitation.dto.InvitationAcceptResponseDto;
import de.upteams.tasktracker.invitation.entity.Invitation;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;

public interface InvitationService {

    InvitationCreationResult createOrRenewInvitation(Project project, String email, ProjectRoles role);

    void processPendingInvitations(String email);

    InvitationAcceptResponseDto acceptInvitation(String inviteToken, AppUser currentUser);


    record InvitationCreationResult(Invitation invitation, boolean registeredUser) {
    }

    record InvitationAcceptanceResult(Invitation invitation, Collaborator collaborator) {
    }


}