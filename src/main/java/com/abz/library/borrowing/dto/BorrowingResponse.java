package com.abz.library.borrowing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.abz.library.borrowing.entity.BorrowingStatus;

public record BorrowingResponse(
    Long id,
    BorrowingStatus status,
    CustomerInfo customer,
    BookInfo bookCopy,
    LocalDateTime borrowedAt,
    LocalDateTime dueDate,
    LocalDateTime returnedAt,
    BigDecimal fineAmount
) {
    public record CustomerInfo(Long id, String firstName, String lastName, String email){

    }

    public record BookInfo(Long id, String copyNumber, Long bookId, String isbn, String bookTitle){

    }
}
