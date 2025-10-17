package de.upteams.tasktracker.user.passwordreset.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordResetRequestDto {

    @Schema(description = "Email пользователя, на который будет отправлена ссылка для сброса пароля",
            example = "user@example.com")
    @NotBlank
    @Email
    private String email;

}
