package com.abz.library.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abz.library.auth.dto.AuthResponse;
import com.abz.library.auth.dto.LoginRequest;
import com.abz.library.auth.dto.RegisterRequest;
import com.abz.library.auth.service.AuthService;
import com.abz.library.user.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping (path = "/register")
    @ResponseStatus (HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest registerRequest){
        return authService.register(registerRequest);
    }

    @PostMapping (path = "/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }
}
