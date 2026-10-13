package com.abz.library.collection.dto;

public record BookSearchCriteria(
    String isbn,
    String title,
    String author,
    String language,
    String department,
    String category,
    Boolean available
) {
}
