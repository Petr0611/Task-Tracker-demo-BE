package de.upteams.tasktracker.user.service.impl;

import de.upteams.tasktracker.user.dto.UserUpdateDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.exception.UserNotFoundException;
import de.upteams.tasktracker.user.persistence.UserRepository;
import de.upteams.tasktracker.user.service.UserService;
import de.upteams.tasktracker.user.util.AppUserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for various operations with Users
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final AppUserMapper mappingService;

    @Override
    public AppUser saveOrUpdate(final AppUser user) {
        return repository.save(user);
    }

    @Override
    @Transactional
    public Optional<AppUser> getByEmail(String email) {
        return repository.findByEmailIgnoreCase(email);
    }

    @Override
    @Transactional
    public AppUser getByEmailOrThrow(String email) {
        return getByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    @Transactional
    public AppUser getByIdOrThrow(String id) {
        return repository
                .findById(UUID.fromString(id))
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public List<UserResponseDto> getAll() {
        return repository
                .findAll()
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    // -------------------------
    // Обновление профиля
    // -------------------------

    @Override
    @Transactional
    public AppUser updateUser(UserUpdateDto dto) {
        String currentUserId = getCurrentUserId();
        return updateProfile(currentUserId, dto);
    }

    @Override
    @Transactional
    public AppUser updateUserById(String id, UserUpdateDto dto) {
        return updateProfile(id, dto);
    }

    @Transactional
    public AppUser updateProfile(String userId, UserUpdateDto updateDto) {
        AppUser user = getByIdOrThrow(userId);

        if (updateDto.displayName() != null) user.setDisplayName(updateDto.displayName());
        if (updateDto.position() != null) user.setPosition(updateDto.position());
        if (updateDto.department() != null) user.setDepartment(updateDto.department());
        if (updateDto.avatarUrl() != null) user.setAvatarUrl(updateDto.avatarUrl());
        if (updateDto.bio() != null) user.setBio(updateDto.bio());

        return repository.save(user);
    }

    /**
     * Получение текущего пользователя из Spring Security контекста
     */


    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new UserNotFoundException("Current user not found in context");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            // напрямую через UserRepository
            AppUser user = repository.findByEmailIgnoreCase(userDetails.getUsername())
                    .orElseThrow(() -> new UserNotFoundException("Current user not found in DB"));
            return user.getId().toString();
        }

        throw new UserNotFoundException("Current user not found in context");
    }
}
