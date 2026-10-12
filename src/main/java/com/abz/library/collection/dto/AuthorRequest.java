package com.abz.library.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(
    @NotBlank (message = "Author name is required")
    @Size (max = 255, message = "Author name must not exceed {max} characters")
    String name
) {
}