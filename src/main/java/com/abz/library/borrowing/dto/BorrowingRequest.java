package com.abz.library.borrowing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record BorrowingRequest(
    @Schema (description = "The Id of the required book to borrow", example = "9")
    @NotNull 
    Long bookId
) {
}
