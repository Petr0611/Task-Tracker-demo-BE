package de.upteams.tasktracker.invitation.entity;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;

@Entity
@Table(name = "project_invitation")
@Getter
@Setter
@NoArgsConstructor
public class Invitation extends BaseEntity {

    @ManyToOne(optional = false)
    private Project project;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ProjectRoles role;

    @NotBlank
    @Column(name = "invite_token", nullable = false, unique = true, length = 40)
    private String inviteToken;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Override
    public String toString() {
        return "Invitation{" +
                "id=" + getId() +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", inviteToken='" + inviteToken + '\'' +
                ", expiresAt=" + expiresAt +
                ", status=" + status +
                ", project=" + getIdForToString(project) +
                '}';
    }
}