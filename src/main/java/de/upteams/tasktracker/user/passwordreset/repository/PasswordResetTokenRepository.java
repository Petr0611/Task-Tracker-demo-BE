package de.upteams.tasktracker.user.passwordreset.repository;

import de.upteams.tasktracker.user.passwordreset.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);


}
