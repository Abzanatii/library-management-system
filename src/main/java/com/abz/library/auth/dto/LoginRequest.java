package com.abz.library.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @Schema (example = "ahmed@example.com")
    @NotBlank (message = "Email is required")
    @Email (message = "Invalid email format")
    String email,

    @Schema (example = "secretPassword")
    @NotBlank (message = "Password is required")
    @Size (min = 8, max = 72,message = "Password must be more then {min} characters")
    String password
) {

}
