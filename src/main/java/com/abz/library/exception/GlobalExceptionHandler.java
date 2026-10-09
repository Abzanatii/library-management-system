package com.abz.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@RestControllerAdvice 
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler{

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String msg, HttpServletRequest req){
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), msg, req.getRequestURI()));
    }

    @ExceptionHandler (Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request){
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An expected error occurred.", request);
    }
    
    @ExceptionHandler (ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request){
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler (BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request){
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler (BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request){
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler (ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request){
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler (AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccssDenied(AccessDeniedException ex, HttpServletRequest request){
        return build(HttpStatus.FORBIDDEN, "Access Denied: " + ex.getMessage(), request);
    }

    @ExceptionHandler (AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request){
        String msg = ex instanceof BadCredentialsException ? "Invalid Email or Password." : "Authentication Failed.";  
        return build(HttpStatus.UNAUTHORIZED, msg, request);
    }
}
