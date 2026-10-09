package com.abz.library.auth.dto;

import com.abz.library.user.entity.Role;

public record AuthResponse(
    String accessToken,
    long expiresInSeconds,
    String email,
    Role role){

}
