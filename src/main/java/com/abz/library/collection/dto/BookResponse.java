package com.abz.library.collection.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BookResponse(
    Long id,
    String isbn,
    String title,
    String description,
    String publisher,
    String publicationYear,
    String language,
    DepartmentResponse department,
    List<AuthorResponse> authors,
    List<CategoryResponse> categories,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
