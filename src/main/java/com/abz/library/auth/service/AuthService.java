package com.abz.library.auth.service;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abz.library.auth.dto.AuthResponse;
import com.abz.library.auth.dto.LoginRequest;
import com.abz.library.auth.dto.RegisterRequest;
import com.abz.library.exception.ConflictException;
import com.abz.library.security.AuthenticatedUser;
import com.abz.library.security.JwtService;
import com.abz.library.user.dto.UserResponse;
import com.abz.library.user.entity.Role;
import com.abz.library.user.entity.User;
import com.abz.library.user.mapper.UserMapper;
import com.abz.library.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional 
    public UserResponse register(RegisterRequest req){
        String email = normalizeEmail(req.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }

        User customer = User.builder()
            .firstName(req.firstName().trim())
            .lastName(req.lastName().trim())
            .email(email)
            .phone(toNull(req.phone()))
            .passwordHash(passwordEncoder.encode(req.password()))
            .dateOfBirth(req.dateOfBirth())
            .role(Role.CUSTOMER)
            .build();
        return userMapper.toResponse(userRepository.save(customer));
    }

    public AuthResponse login(LoginRequest req){
        String email = normalizeEmail(req.email());
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, req.password()));
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return new AuthResponse(
            jwtService.generateToken(user),
            jwtService.getExpirationSeconds(),
            user.email(),
            user.role());
    }


    private static String normalizeEmail(String email){
        return email.trim().toLowerCase(Locale.ROOT);
    }
    
    private static String toNull(String value){
        return value == null || value.isBlank() ? null : value;
    }
}
