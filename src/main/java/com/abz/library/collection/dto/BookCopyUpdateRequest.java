package com.abz.library.collection.dto;

import com.abz.library.collection.entity.CopyStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookCopyUpdateRequest(
    @NotBlank 
    @Size (max = 20, message = "Copy number must not exceed {max} characters")
    @Pattern (regexp = "^[A-Za-z0-9-]+$", message = "Copy number only allows letters, digits and hyphens")
    String copyNumber,

    @Schema (description = "New status", example = "MAINTENANCE")
    @NotNull 
    CopyStatus status
) {
}
