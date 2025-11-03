package de.upteams.tasktracker.user.service;

import de.upteams.tasktracker.user.dto.UserUpdateDto;
import de.upteams.tasktracker.user.dto.request.ChangePasswordRequestDTO;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface UserService {
    ResponseEntity<String> changePasswordByAdmin( String id, ChangePasswordRequestDTO request, Authentication authentication);

    ResponseEntity<String> changePassword( ChangePasswordRequestDTO request, Authentication authentication);

    AppUser saveOrUpdate(AppUser user);

    Optional<AppUser> getByEmail(String email);

    AppUser getByEmailOrThrow(String email);

    AppUser getByIdOrThrow(String id);

    List<UserResponseDto> getAll();

    // Новый метод — обновление текущего пользователя
    AppUser updateUser(UserUpdateDto dto);

    // Новый метод — обновление пользователя админом по ID
    AppUser updateUserById(String id, UserUpdateDto dto);
}
