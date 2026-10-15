package com.abz.library.reservation.dto;

import java.time.LocalDateTime;

import com.abz.library.reservation.entity.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude (JsonInclude.Include.NON_NULL)
public record ReservationResponse(
    Long id,
    Long customerId,
    String customerName,
    Long bookId,
    String bookTitle,
    ReservationStatus status,
    Integer queuePosition,
    LocalDateTime reservedAt,
    LocalDateTime expiresAt
) {
}
