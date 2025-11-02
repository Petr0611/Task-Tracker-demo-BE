package de.upteams.tasktracker.user.controller.impl;

import de.upteams.tasktracker.user.controller.interfaces.UserApi;
import de.upteams.tasktracker.user.dto.UserUpdateDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller that receives http-requests for various operations with Employees
 */
@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserApi {

    private final UserService service;

    @Override
    public List<UserResponseDto> getAll() {
        return service.getAll();
    }

    /**
     * Returns the current authenticated user
     */
    @Override
    public UserResponseDto getCurrentUser(Authentication authentication) {
        AppUser user = service.getByEmailOrThrow(authentication.getName());
        return mapToDto(user);
    }

    /**
     * Returns the user by id
     */
    @Override
    public UserResponseDto getById(String id, Authentication authentication) {
        AppUser requestedUser = service.getByIdOrThrow(id);
        return mapToDto(requestedUser);
    }


    /**
     * Updates profile of the current authenticated user
     */
    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public UserResponseDto updateUser(@RequestBody UserUpdateDto dto) {
        AppUser updated = service.updateUser(dto);
        return mapToDto(updated);
    }

    /**
     * Updates user profile by ID (admin only)
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto updateUserById(@PathVariable String id, @RequestBody UserUpdateDto dto) {
        AppUser updated = service.updateUserById(id, dto);
        return mapToDto(updated);
    }

    /**
     * Helper to convert AppUser entity to full UserResponseDto
     */
    private UserResponseDto mapToDto(AppUser user) {
        return new UserResponseDto(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name(),
                user.getConfirmationStatus(),
                user.getDisplayName(),
                user.getPosition(),
                user.getDepartment(),
                user.getAvatarUrl(),
                user.getBio()
        );
    }
}
