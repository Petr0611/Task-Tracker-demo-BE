package de.upteams.tasktracker.user.passwordreset.entity;

import de.upteams.tasktracker.user.entity.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne
    private AppUser user;

    // хранит токен сброса пароля,
    // связанный с пользователем и сроком действия.

    private LocalDateTime expireDate;

}
