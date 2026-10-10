package com.abz.library.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
    @NotBlank (message = "Department name is required") 
    @Size (max = 50, message = "Maximum {max} characters is allowed")
    String name,

    @Size (max = 300, message = "Maximum {max} characters is allowed")
    String description
) {
}
