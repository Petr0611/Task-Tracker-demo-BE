package de.upteams.tasktracker.user.passwordreset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// используется для подтверждения сброса пароля (пользователь вводит токен и новый пароль).

@Setter
@Getter
public class PasswordResetDto {


    @Schema(
            description = "Токен сброса пароля, полученный на email",
            example = "f3a9b1d2-4c5e-4e8a-9a2c-1a2b3c4d5e6f"
    )
    @NotBlank
    private String token;


    @Schema(
            description = "Новый пароль пользователя. Минимум 8 символов.",
            example = "NewSecurePassword123"
    )
    @NotBlank
    @Size(min = 8)
    private String newPassword;

}
