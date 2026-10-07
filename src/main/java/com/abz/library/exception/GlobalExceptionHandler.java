package com.abz.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
// import org.springframework.web.context.request.ServletWebRequest;
// import org.springframework.web.context.request.WebRequest;
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

    @ExceptionHandler (ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest request){
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler (UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request){
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }
    
    // private static String path(WebRequest request){
    //     return request instanceof ServletWebRequest req ? req.getRequest().getRequestURI(): ""; 
    // }

}
