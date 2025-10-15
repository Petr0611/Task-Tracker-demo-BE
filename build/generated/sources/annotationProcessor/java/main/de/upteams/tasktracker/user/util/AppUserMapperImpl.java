package de.upteams.tasktracker.user.util;

import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-11T20:43:53+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.13.jar, environment: Java 17.0.16 (Amazon.com Inc.)"
)
@Component
public class AppUserMapperImpl implements AppUserMapper {

    @Override
    public UserResponseDto mapEntityToDto(AppUser entity) {
        if ( entity == null ) {
            return null;
        }

        String email = null;
        String role = null;
        ConfirmationStatus confirmationStatus = null;

        email = entity.getEmail();
        if ( entity.getRole() != null ) {
            role = entity.getRole().name();
        }
        confirmationStatus = entity.getConfirmationStatus();

        UserResponseDto userResponseDto = new UserResponseDto( email, role, confirmationStatus );

        return userResponseDto;
    }
}
