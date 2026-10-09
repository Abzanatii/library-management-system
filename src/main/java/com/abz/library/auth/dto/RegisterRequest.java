package com.abz.library.auth.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank (message = "First name required")
    @Size (max = 30, message = "Maximum {30} characters is allowed")
    String firstName,

    @NotBlank (message = "Last name required")
    @Size (max = 30, message = "Maximum {30} characters is allowed")
    String lastName,

    @NotBlank (message = "Email is required")
    @Email (message = "Invalid email format")
    String email,
    @Pattern (regexp = "^\\d{11}$", message = "Invalid phone number")
    String phone,

    @NotBlank (message = "Password is required")
    @Size (min = 8, max = 72,message = "Password must be more then {min} characters")
    String password,

    @Past (message = "Date of birth must be in the past")
    @NotNull (message = "Date of birth is required")
    LocalDate dateOfBirth
) {

}
