package de.upteams.tasktracker.user.passwordreset.entity;

import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "password_reset_token")
public class PasswordResetToken extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    private AppUser user;

    private LocalDateTime expireDate;

    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "token='" + token + '\'' +
                ", user=" + (user != null ? user.getId() : null) +
                ", expireDate=" + expireDate +
                '}';
    }
}
