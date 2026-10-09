package com.abz.library.user.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.abz.library.user.entity.Role;
import com.abz.library.user.entity.UserStatus;

public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phone,
    LocalDate dateOfBirth,
    Role role,
    UserStatus status,
    LocalDateTime createdAt
) {

}
