package com.abz.library.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank (message = "Category name is required")
    @Size (max = 50, message = "Category name must not exceed {max} characters")
    String name
) {
}
