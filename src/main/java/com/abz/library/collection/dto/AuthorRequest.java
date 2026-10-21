package com.abz.library.collection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(
    @Schema (example = "Robert C. Rito")
    @NotBlank (message = "Author name is required")
    @Size (max = 255, message = "Author name must not exceed {max} characters")
    String name
) {
}