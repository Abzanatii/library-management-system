package com.abz.library.exception;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ErrorResponse(
    LocalDateTime timeStamp,
    int status,
    String message,
    String path,
    @JsonInclude (JsonInclude.Include.NON_EMPTY) List<FieldError> fieldErrors){

    public static ErrorResponse of(int status, String message, String path, List<FieldError> fieldErrors){
        return new ErrorResponse(LocalDateTime.now(), status, message, path, fieldErrors);
    }

    public static ErrorResponse of(int status, String message, String path){
        return of(status, message, path, null);
    }

    public record FieldError(String field, String message){

    }
}
