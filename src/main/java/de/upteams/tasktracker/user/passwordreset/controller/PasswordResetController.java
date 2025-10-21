package de.upteams.tasktracker.user.passwordreset.controller;


import de.upteams.tasktracker.user.passwordreset.dto.PasswordResetDto;
import de.upteams.tasktracker.user.passwordreset.dto.request.PasswordResetRequestDto;
import de.upteams.tasktracker.user.passwordreset.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService service;

    @Operation(summary = "Запрос на сброс пароля",
            description = "Отправляет токен на email пользователя")

    @ApiResponse(responseCode = "200",
            description = "Ссылка отправлена")

    @PostMapping("/reset-password-request")
    public ResponseEntity<?> requestReset(@RequestBody @Valid PasswordResetRequestDto dto) {
        service.initialPasswordReset(dto.getEmail());
        return ResponseEntity.ok("Reset link sent if email exist.");
    }

    @Operation(
            summary = "Сброс пароля по токену",
            description = "Позволяет пользователю сбросить пароль, используя токен, полученный на email. Токен должен быть действительным и не истекшим."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пароль успешно обновлён"),
            @ApiResponse(responseCode = "400", description = "Невалидный токен или пароль"),
            @ApiResponse(responseCode = "404", description = "Токен не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetDto dto) {
        service.resetPassword(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.ok("Password successfully updated.");
    }

}
