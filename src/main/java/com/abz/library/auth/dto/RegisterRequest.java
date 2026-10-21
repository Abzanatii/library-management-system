package com.abz.library.auth.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @Schema (example = "Ahmed")
    @NotBlank (message = "First name required")
    @Size (max = 30, message = "Maximum {30} characters is allowed")
    String firstName,

    @Schema (example = "Yusef")
    @NotBlank (message = "Last name required")
    @Size (max = 30, message = "Maximum {30} characters is allowed")
    String lastName,

    @Schema (description = "Login email (unique)",
        example = "Ahmed@example.com")
    @NotBlank (message = "Email is required")
    @Email (message = "Invalid email format")
    String email,

    @Schema (description = "Must be 11 digits", example = "01123967481")
    @Pattern (regexp = "^\\d{11}$", message = "Invalid phone number")
    String phone,

    @Schema (example = "secretPassword")
    @NotBlank (message = "Password is required")
    @Size (min = 8, max = 72,message = "Password must be more then {min} characters")
    String password,

    @Schema (description = "Must be in the past", example = "1999-02-12")
    @Past (message = "Date of birth must be in the past")
    @NotNull (message = "Date of birth is required")
    LocalDate dateOfBirth
) {

}
