package de.upteams.tasktracker.user.service;

import de.upteams.tasktracker.user.dto.UserUpdateDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;
import java.util.Optional;

public interface UserService {

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
