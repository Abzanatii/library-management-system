package com.abz.library.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
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

    @ExceptionHandler (DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request){
        log.warn("Data integrity violation using {} {}: {}", 
        request.getMethod(), request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Request conflicts with existing data", request);
    }

    @ExceptionHandler (ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request){
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations().stream()
            .map(violation -> {
                String path = violation.getPropertyPath().toString();
                return new ErrorResponse.FieldError(
                path.substring(path.lastIndexOf('.') + 1), 
                violation.getMessage());
            }).toList();
        return ResponseEntity.badRequest().body(ErrorResponse.of(400, "Validation failed", 
            request.getRequestURI(), fieldErrors));
    }

    @Override 
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request){

            List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
            ex.getParameterValidationResults().forEach(result -> {
                String name = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(error ->
                    fieldErrors.add(new ErrorResponse.FieldError(name, error.getDefaultMessage())));
            });

            String path = ((ServletWebRequest) request).getRequest().getRequestURI();
            return  ResponseEntity.status(status).headers(headers)
                    .body(ErrorResponse.of(status.value(), "Validation failed", path, fieldErrors));
    }

    @Override 
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, 
        HttpHeaders headers, HttpStatusCode status, WebRequest request){

            List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();

            ex.getBindingResult().getFieldErrors().forEach(
                error -> fieldErrors.add(new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage())));
            
            ex.getBindingResult().getGlobalErrors().forEach(
                error -> fieldErrors.add(new ErrorResponse.FieldError(error.getObjectName(), error.getDefaultMessage())));
            
            String path = ((ServletWebRequest) request).getRequest().getRequestURI();
            return  ResponseEntity.status(status).headers(headers)
                    .body(ErrorResponse.of(status.value(), "Validation failed", path, fieldErrors));
    }

    @Override 
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request){

            String path = ((ServletWebRequest) request).getRequest().getRequestURI();
            return  ResponseEntity.status(status).headers(headers)
                    .body(ErrorResponse.of(status.value(), "Malformed or unreadable request body", path));
    }

    @Override 
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request){

            String name = ex instanceof MethodArgumentTypeMismatchException arg ? arg.getName() : ex.getPropertyName();
            String path = ((ServletWebRequest) request).getRequest().getRequestURI();
            return ResponseEntity.status(status).headers(headers)
                    .body(ErrorResponse.of(status.value(),
                    "Invalid value '" + ex.getValue() + "' expected '" + name + "'", path));
    }

    @Override 
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object o,
        HttpHeaders headers, HttpStatusCode status, WebRequest request){

            HttpStatus resolve = HttpStatus.resolve(status.value());
            String path = ((ServletWebRequest) request).getRequest().getRequestURI();
            String message = o instanceof ProblemDetail pro && pro.getDetail() != null
                ? pro.getDetail() : resolve != null
                ? resolve.getReasonPhrase() : "An unexpected error occurred";
            return ResponseEntity.status(status).headers(headers)
                    .body(ErrorResponse.of(status.value(), message, path));
    }
}
