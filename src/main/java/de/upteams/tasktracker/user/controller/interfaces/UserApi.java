package de.upteams.tasktracker.user.controller.interfaces;

import de.upteams.tasktracker.user.dto.UserUpdateDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST mappings for user operations.
 * Implementation classes should implement this interface.
 */
@RequestMapping("/api/v1/users")
public interface UserApi extends UserApiSwaggerDoc {

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    List<UserResponseDto> getAll();

    /**
     * Возвращает текущего пользователя
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    UserResponseDto getCurrentUser(Authentication authentication);

    /**
     * Возвращает пользователя по email
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    UserResponseDto getById(@PathVariable String id);

    /**
     * Обновляет профиль текущего пользователя
     */
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    UserResponseDto updateUser(@RequestBody UserUpdateDto dto);

    /**
     * Обновляет пользователя по ID (только для администратора)
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    UserResponseDto updateUserById(@PathVariable String id, @RequestBody UserUpdateDto dto);
}
