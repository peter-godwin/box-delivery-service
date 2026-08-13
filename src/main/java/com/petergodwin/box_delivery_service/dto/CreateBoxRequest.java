package com.petergodwin.box_delivery_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBoxRequest(

        @NotBlank(message = "txref is required")
        @Size(max = 20, message = "txref must not exceed 20 characters")
        String txref,

        @Min(value = 1, message = "weightLimit must be greater than 0")
        @Max(value = 500, message = "weightLimit must not exceed 500 grams")
        Integer weightLimit,

        @Min(value = 0, message = "batteryLevel cannot be below 0")
        @Max(value = 100, message = "batteryLevel cannot exceed 100")
        Integer batteryLevel

) {
}