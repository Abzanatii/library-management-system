package com.abz.library.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@ConfigurationProperties (prefix = "jwt")
@Validated 
public record JwtProperties(
    @NotBlank 
    String secret,

    @DefaultValue ("60")
    @Positive 
    Long expirationMinutes
) {

}
