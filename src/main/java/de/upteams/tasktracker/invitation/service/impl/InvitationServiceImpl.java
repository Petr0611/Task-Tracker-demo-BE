package de.upteams.tasktracker.invitation.service.impl;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.exception.CollaboratorAlreadyExistsException;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.OwnerAlreadyExistsException;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.invitation.dto.InvitationAcceptResponseDto;
import de.upteams.tasktracker.invitation.entity.Invitation;
import de.upteams.tasktracker.invitation.entity.InvitationStatus;
import de.upteams.tasktracker.invitation.persistence.InvitationRepository;
import de.upteams.tasktracker.invitation.service.interfaces.InvitationService;
import de.upteams.tasktracker.mail.EmailService;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationServiceImpl implements InvitationService {

    private static final Duration INVITATION_TTL = Duration.ofHours(72);

    private final InvitationRepository invitationRepository;
    private final CollaboratorService collaboratorService;
    private final UserService userService;
    private final EmailService emailService;
    private final Clock clock;

    @Override
    @Transactional
    public InvitationCreationResult createOrRenewInvitation(Project project, String email, ProjectRoles role) {
        String normalizedEmail = normalizeEmail(email);
        Instant newExpiry = Instant.now(clock).plus(INVITATION_TTL);

        userService.getByEmail(normalizedEmail).ifPresent(existingUser -> {
            boolean alreadyCollaborator =
                    collaboratorService.getCollaborator(existingUser, project).isPresent();
            if (alreadyCollaborator) {
                throw new CollaboratorAlreadyExistsException();
            }
        });

        if (role == ProjectRoles.OWNER &&
                collaboratorService.projectHasRole(project, ProjectRoles.OWNER)) {
            throw new OwnerAlreadyExistsException();
        }

        Invitation invitation = invitationRepository
                .findByProjectAndEmailAndStatus(project, normalizedEmail, InvitationStatus.PENDING)
                .map(existing -> refreshExistingInvitation(existing, newExpiry, role))
                .orElseGet(() -> createInvitation(project, normalizedEmail, role, newExpiry));

        invitation = invitationRepository.save(invitation);

        boolean registeredUser = userService.getByEmail(normalizedEmail).isPresent();
        if (registeredUser) {
            emailService.sendProjectInvitationForExistingUser(normalizedEmail, project.getTitle(), invitation.getInviteToken());
        } else {
            emailService.sendProjectInvitationForNewUser(normalizedEmail, project.getTitle(), invitation.getInviteToken());
        }

        log.info("Invitation {} prepared for project {} and email {}", invitation.getId(), project.getId(), normalizedEmail);
        return new InvitationCreationResult(invitation, registeredUser);
    }

    @Override
    @Transactional
    public void processPendingInvitations(String email) {
        String normalizedEmail = normalizeEmail(email);
        Instant now = Instant.now(clock);

        List<Invitation> invitations = invitationRepository.findAllByEmailAndStatus(normalizedEmail, InvitationStatus.PENDING);
        AppUser user = userService.getByEmailOrThrow(normalizedEmail);

        invitations.stream()
                .filter(invitation -> !invitation.getExpiresAt().isBefore(now))
                .forEach(invitation -> activateCollaborator(user, invitation));
    }

    @Override
    @Transactional
    public InvitationAcceptResponseDto acceptInvitation(String inviteToken, AppUser currentUser) {
        if (inviteToken == null || inviteToken.isBlank()) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invitation token is required");
        }

        Invitation invitation = invitationRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new RestApiException(HttpStatus.BAD_REQUEST, "Invitation token is invalid"));

        Instant now = Instant.now(clock);

        if (!InvitationStatus.PENDING.equals(invitation.getStatus())) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invitation token has already been used or cancelled");
        }

        if (invitation.getExpiresAt().isBefore(now)) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invitation token has expired");
        }

        // 🔐 Проверка: токен должен быть принят только тем, кому он адресован
        if (!invitation.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "This invitation is not intended for your account");
        }

        Collaborator collaborator = activateCollaborator(currentUser, invitation);

        return new InvitationAcceptResponseDto(
                invitation.getProject().getId().toString(),
                invitation.getProject().getTitle(),
                collaborator.getStatus(),
                invitation.getRole()
        );
    }


//    public InvitationAcceptanceResult acceptInvitation(String inviteToken, AppUser currentUser) {
//        if (inviteToken == null || inviteToken.isBlank()) {
//            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invitation token is invalid or expired");
//        }
//
//        Invitation invitation = invitationRepository.findByInviteToken(inviteToken)
//                .orElseThrow(() -> new RestApiException(HttpStatus.BAD_REQUEST, "Invitation token is invalid or expired"));
//
//        Instant now = Instant.now(clock);
//        if (!InvitationStatus.PENDING.equals(invitation.getStatus()) || invitation.getExpiresAt().isBefore(now)) {
//            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invitation token is invalid or expired");
//        }
//
//        String normalizedEmail = normalizeEmail(currentUser.getEmail());
//        if (!normalizedEmail.equals(invitation.getEmail())) {
//            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invitation token is invalid or expired");
//        }
//
//        Collaborator collaborator = activateCollaborator(currentUser, invitation);
//        return new InvitationAcceptanceResult(invitation, collaborator);
//    }

    private Invitation refreshExistingInvitation(Invitation invitation, Instant newExpiry, ProjectRoles role) {
        invitation.setExpiresAt(newExpiry);
        invitation.setRole(role);
        return invitation;
    }

    private Invitation createInvitation(Project project, String email, ProjectRoles role, Instant expiresAt) {
        Invitation invitation = new Invitation();
        invitation.setProject(project);
        invitation.setEmail(email);
        invitation.setRole(role);
        invitation.setInviteToken(UUID.randomUUID().toString());
        invitation.setExpiresAt(expiresAt);
        invitation.setStatus(InvitationStatus.PENDING);
        return invitation;
    }

    private Collaborator activateCollaborator(AppUser user, Invitation invitation) {
        Set<ProjectRoles> roles = EnumSet.of(invitation.getRole());

        Collaborator collaborator = collaboratorService.activateCollaborator(
                user,
                invitation.getProject(),
                roles
        );

        invitation.setStatus(InvitationStatus.USED);
        invitationRepository.save(invitation);

        log.info("Invitation {} consumed for project {}", invitation.getId(), invitation.getProject().getId());
        log.info("Saving collaborator: user={}, project={}, roles={}", user.getEmail(), invitation.getProject().getId(), roles);

        return collaborator;
    }


    private String normalizeEmail(String email) {
        return email.toLowerCase(Locale.ROOT).trim();
    }
}