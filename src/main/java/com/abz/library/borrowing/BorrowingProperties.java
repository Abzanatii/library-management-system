package com.abz.library.borrowing;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;

@Validated 
@ConfigurationProperties (prefix = "borrowing")
public record BorrowingProperties(
    @Positive 
    int maxBorrow,

    @Positive 
    BigDecimal maxFines,

    @Positive 
    int PeriodDays,

    @Positive 
    BigDecimal perDayFine
) {
}
