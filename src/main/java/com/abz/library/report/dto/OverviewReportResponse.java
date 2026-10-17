package com.abz.library.report.dto;

import java.math.BigDecimal;

public record OverviewReportResponse(
    long totalBooks,
    long totalCopies,
    long availableCopies,
    long borrowedCopies,
    long damagedCopies,
    long totalCustomers,
    long activeBorrowings,
    long overdueBorrowings,
    BigDecimal unpaidFines
) {
}
