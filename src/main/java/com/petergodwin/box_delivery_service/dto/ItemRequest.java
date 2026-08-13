package com.petergodwin.box_delivery_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record ItemRequest(

        @NotBlank(message = "name is required")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "name may only contain letters, numbers, hyphens and underscores"
        )
        String name,

        @Positive(message = "weight must be greater than 0")
        Integer weight,

        @NotBlank(message = "code is required")
        @Pattern(
                regexp = "^[A-Z0-9_]+$",
                message = "code may only contain uppercase letters, numbers and underscores"
        )
        String code

) {
}