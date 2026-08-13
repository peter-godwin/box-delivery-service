package com.petergodwin.box_delivery_service.dto;

import com.petergodwin.box_delivery_service.entity.BoxState;

import java.util.UUID;

public record BoxResponse(
        UUID id,
        String txref,
        Integer weightLimit,
        Integer batteryLevel,
        BoxState state
) {
}