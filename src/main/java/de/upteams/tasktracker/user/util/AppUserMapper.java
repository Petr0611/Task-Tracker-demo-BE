package de.upteams.tasktracker.user.util;

import de.upteams.tasktracker.user.dto.EmployeeDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface AppUserMapper {

    UserResponseDto mapEntityToDto(AppUser entity);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(source = "avatarUrl", target = "avatar")
    EmployeeDto mapEntityToEmployeeDto(AppUser entity);
}