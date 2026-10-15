package com.abz.library.fine.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.abz.library.fine.entity.FineStatus;

public record FineResponse(
    Long id,
    Long borrowId,
    String bookTitle,
    BigDecimal amount,
    FineStatus status,
    LocalDateTime createdAt,
    LocalDateTime paidAt
) {
}
