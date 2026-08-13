package com.petergodwin.box_delivery_service.controller;

import com.petergodwin.box_delivery_service.dto.BatteryResponse;
import com.petergodwin.box_delivery_service.dto.BoxResponse;
import com.petergodwin.box_delivery_service.dto.CreateBoxRequest;
import com.petergodwin.box_delivery_service.dto.ItemResponse;
import com.petergodwin.box_delivery_service.dto.LoadItemsRequest;
import com.petergodwin.box_delivery_service.service.BoxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/boxes")
@RequiredArgsConstructor
public class BoxController {

    private final BoxService boxService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoxResponse createBox(
            @Valid @RequestBody CreateBoxRequest request
    ) {
        return boxService.createBox(request);
    }

    @PostMapping("/{boxId}/items")
    public List<ItemResponse> loadBox(
            @PathVariable UUID boxId,
            @Valid @RequestBody LoadItemsRequest request
    ) {
        return boxService.loadBox(boxId, request);
    }

    @GetMapping("/{boxId}/items")
    public List<ItemResponse> getBoxItems(
            @PathVariable UUID boxId
    ) {
        return boxService.getBoxItems(boxId);
    }

    @GetMapping("/available-for-loading")
    public List<BoxResponse> getAvailableBoxes() {
        return boxService.getAvailableBoxes();
    }

    @GetMapping("/{boxId}/battery")
    public BatteryResponse getBatteryLevel(
            @PathVariable UUID boxId
    ) {
        return boxService.getBatteryLevel(boxId);
    }
}