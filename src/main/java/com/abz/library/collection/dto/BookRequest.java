package com.abz.library.collection.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookRequest(
    @NotBlank (message = "ISBN is required")
    @Pattern (regexp = "^[0-9Xx -]{10,17}$", message = "ISBN only allows digits, spaces and hyphens")
    String isbn,
    
    @NotBlank (message = "Title is required")
    @Size (max = 255, message = "Title must not exceed {max} characters")
    String title,

    @Size (max = 2000, message = "Description must not exceed {max} characters")
    String description,

    @Size (max = 100, message = "Publisher must not exceed {max} characters")
    String publisher,

    Integer publicationYear,

    @Size (max = 50, message = "Language must not exceed {max} characters")
    String language,

    @NotNull (message = "The book must be associated with a department")
    Long departmentId,

    @NotEmpty (message = "The book must be associated with at least one author")
    Set<
    @NotNull(message = "Valid author required") 
    Long> authorIds,

    Set<@NotNull Long> categoryIds
) {
}
