package de.upteams.tasktracker.dto;

import de.upteams.tasktracker.user.dto.request.UserCreateDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserCreateDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            ;
            validator = factory.getValidator();
        }
    }

    @Test
    void validPassword_shouldPassValidation() {
        UserCreateDto dto = new UserCreateDto("validuser@mail.com", "StrongPass1!");
        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Expected no validation errors for a valid password");
    }

    @Test
    void shortPassword_shouldFailValidation() {
        UserCreateDto dto = new UserCreateDto("short@mail.com", "Ab1!");
        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Expected validation error for short password");
    }

    @Test
    void passwordWithoutUppercase_shouldFailValidation() {
        UserCreateDto dto = new UserCreateDto("noUpper@mail.com", "lowercase1!");
        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Expected validation error for missing uppercase letter");
    }

    @Test
    void passwordWithoutDigit_shouldFailValidation() {
        UserCreateDto dto = new UserCreateDto("noDigit@mail.com", "NoDigits!");
        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Expected validation error for missing digit");
    }

    @Test
    void passwordWithCyrillic_shouldFailValidation() {
        UserCreateDto dto = new UserCreateDto("cyrillic@mail.com", "Пароль123!");
        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Expected validation error for Cyrillic characters");
    }
}
