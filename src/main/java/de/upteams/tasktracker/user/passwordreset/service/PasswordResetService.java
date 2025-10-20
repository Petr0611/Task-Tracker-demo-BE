package de.upteams.tasktracker.user.passwordreset.service;

public interface PasswordResetService {

    void initialPasswordReset(String email);
    void resetPassword(String token, String newPassword);
}
