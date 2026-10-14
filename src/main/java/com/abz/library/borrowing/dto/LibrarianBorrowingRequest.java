package com.abz.library.borrowing.dto;

import jakarta.validation.constraints.NotNull;

public record LibrarianBorrowingRequest(
    @NotNull 
    Long customerId,

    @NotNull 
    Long bookId
) {
}
