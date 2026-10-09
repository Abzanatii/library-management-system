package com.abz.library.security;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.abz.library.user.entity.Role;
import com.abz.library.user.entity.User;
import com.abz.library.user.entity.UserStatus;

public record AuthenticatedUser(
    Long id,
    String email,
    String passwordHash,
    Role role,
    UserStatus status
) implements UserDetails{

    public static AuthenticatedUser from(User user){
        return new AuthenticatedUser(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole(), user.getStatus());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

}
