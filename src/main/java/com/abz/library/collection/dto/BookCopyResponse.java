package com.abz.library.collection.dto;

import java.time.LocalDateTime;

import com.abz.library.collection.entity.CopyStatus;

public record BookCopyResponse(
    Long id,
    Long bookId,
    String bookTitle,
    String isbn,
    String copyNumber,
    CopyStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
