package de.upteams.tasktracker.invitation.service.impl;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.OwnerAlreadyExistsException;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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

    public InvitationAcceptanceResult acceptInvitation(String inviteToken, AppUser currentUser) {
        if (inviteToken == null || inviteToken.isBlank()) {
            log.warn("Empty or null invitation token received by user {}", currentUser.getEmail());
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invitation token is required");
        }

        // Находим приглашение по токену
        Invitation invitation = invitationRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> {
                    log.warn("Invitation token not found: {}", inviteToken);
                    return new RestApiException(HttpStatus.BAD_REQUEST, "Invitation token is invalid");
                });

        Instant now = Instant.now(clock);

        // Проверяем статус
        if (!InvitationStatus.PENDING.equals(invitation.getStatus())) {
            log.warn("Invitation token {} already used or cancelled. Current status: {}", inviteToken, invitation.getStatus());
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invitation token has already been used or cancelled");
        }

        // Проверяем истечение
        if (invitation.getExpiresAt().isBefore(now)) {
            log.warn("Invitation token {} expired at {}", inviteToken, invitation.getExpiresAt());
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invitation token has expired");
        }

        // Активируем текущего пользователя в проекте
        Collaborator collaborator = activateCollaborator(currentUser, invitation);

        log.info("User {} successfully accepted invitation {} for project {}", currentUser.getEmail(), inviteToken, invitation.getProject().getId());
        return new InvitationAcceptanceResult(invitation, collaborator);
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
        if (invitation.getRole() == ProjectRoles.OWNER) {
            boolean hasOwner = invitation.getProject().getProjectTeam().stream()
                    .anyMatch(collab -> collab.getProjectRolesSet().contains(ProjectRoles.OWNER));

            if (hasOwner) {
                throw new OwnerAlreadyExistsException();
            }
        }
        Collaborator collaborator = collaboratorService.activateCollaborator(
                user,
                invitation.getProject(),
                EnumSet.of(invitation.getRole())
        );

        invitation.setStatus(InvitationStatus.USED);
        invitationRepository.save(invitation);
        log.info("Invitation {} consumed for project {}", invitation.getId(), invitation.getProject().getId());
        return collaborator;
    }

    private String normalizeEmail(String email) {
        return email.toLowerCase(Locale.ROOT).trim();
    }
}