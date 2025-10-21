package de.upteams.tasktracker.user.passwordreset.service;

import de.upteams.tasktracker.mail.EmailService;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.passwordreset.entity.PasswordResetToken;
import de.upteams.tasktracker.user.passwordreset.repository.PasswordResetTokenRepository;
import de.upteams.tasktracker.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService{

    private final UserService userService;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;


    @Transactional
    @Override
    public void initialPasswordReset(String email) {
        AppUser user = userService.getByEmailOrThrow(email.toLowerCase().trim());
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpireDate(LocalDateTime.now().plusHours(1));

        tokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), token);

    }

    @Transactional
    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpireDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        AppUser user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveOrUpdate(user);
        tokenRepository.delete(resetToken);

    }
}
