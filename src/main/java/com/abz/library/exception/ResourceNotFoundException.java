package com.abz.library.exception;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message){
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id){
        super(resource + " with ID: " + id + " is not found");
    }
}
