package com.abz.library.report.dto;

public record MostBorrowedResponse(
    Long bookId,
    String isbn,
    String title,
    long borrowCount
) {
}
