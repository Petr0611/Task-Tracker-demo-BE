package de.upteams.tasktracker.invitation.persistence;

import de.upteams.tasktracker.invitation.entity.Invitation;
import de.upteams.tasktracker.invitation.entity.InvitationStatus;
import de.upteams.tasktracker.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    Optional<Invitation> findByProjectAndEmailAndStatus(Project project, String email, InvitationStatus status);

    List<Invitation> findAllByEmailAndStatus(String email, InvitationStatus status);

    Optional<Invitation> findByInviteToken(String inviteToken);
}
