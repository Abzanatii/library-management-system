package com.abz.library.reservation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;

@Validated 
@ConfigurationProperties (prefix = "reservation")
public record ReservationProperties(
    @Positive int expiryHours) {
}
