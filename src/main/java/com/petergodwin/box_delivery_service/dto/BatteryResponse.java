package com.petergodwin.box_delivery_service.dto;

import java.util.UUID;

public record BatteryResponse(
        UUID boxId,
        Integer batteryLevel
) {
}