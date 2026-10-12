package com.abz.library.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookCopyCreateRequest(
    @NotNull (message = "The book copy must have a book ID")
    Long bookId,

    @NotBlank 
    @Size (max = 20, message = "Copy number must not exceed {max} characters")
    @Pattern (regexp = "^[A-Za-z0-9-]+$", message = "Copy number only allows letters, digits and hyphens")
    String copyNumber
) {
}
