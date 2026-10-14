package com.abz.library.borrowing.dto;

import jakarta.validation.constraints.NotNull;

public record BorrowingRequest(
    @NotNull 
    Long bookId
) {
}
