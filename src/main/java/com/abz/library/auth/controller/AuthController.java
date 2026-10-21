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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag (name = "Authentication", description = "User login and registration")
@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation (summary = "Customer registration",
        description = "Creates a CUSTOMER account, can not create an ADMIN or LIBRARIAN account.")
    @ResponseStatus (HttpStatus.CREATED)
    @PostMapping (path = "/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest registerRequest){
        return authService.register(registerRequest);
    }

    @Operation (summary = "User login", description = "Athenticates the user and returns the JWT.")
    @PostMapping (path = "/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }
}
