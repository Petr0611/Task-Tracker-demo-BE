package de.upteams.tasktracker.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateDto(
        @Schema(
                description = "new User email",
                example = "tes_dev@upteams.de"
        )
        String email,

        @Schema(
                description = "new User password",
                example = "dev_TR_pass_007"
        )
        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, message = "Password must contain at least 8 characters")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>?,./])[A-Za-z\\d!@#$%^&*()_+\\-={}\\[\\]:;\"'<>?,./]{8,}$",
                message = """
                        Password must include:
                        - at least one uppercase letter,
                        - one lowercase letter,
                        - one digit,
                        - one special character,
                        - and only Latin letters.
                        """
        )
        String password) {
}
