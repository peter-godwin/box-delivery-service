package com.petergodwin.box_delivery_service.dto;

import java.util.UUID;

public record ItemResponse(
        UUID id,
        String name,
        Integer weight,
        String code
) {
}