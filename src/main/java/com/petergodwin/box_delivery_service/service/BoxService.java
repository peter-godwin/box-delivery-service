package com.petergodwin.box_delivery_service.service;

import com.petergodwin.box_delivery_service.dto.*;
import com.petergodwin.box_delivery_service.entity.Box;
import com.petergodwin.box_delivery_service.entity.BoxState;
import com.petergodwin.box_delivery_service.entity.Item;
import com.petergodwin.box_delivery_service.exception.BoxLoadingException;
import com.petergodwin.box_delivery_service.exception.BoxNotFoundException;
import com.petergodwin.box_delivery_service.repository.BoxRepository;
import com.petergodwin.box_delivery_service.repository.ItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoxService {

    private static final int MINIMUM_LOADING_BATTERY = 25;

    private final BoxRepository boxRepository;
    private final ItemRepository itemRepository;

    public BoxResponse createBox(CreateBoxRequest request) {

        if (boxRepository.existsByTxref(request.txref())) {
            throw new BoxLoadingException("A box with this txref already exists");
        }

        Box box = new Box();
        box.setTxref(request.txref());
        box.setWeightLimit(request.weightLimit());
        box.setBatteryLevel(request.batteryLevel());
        box.setState(BoxState.IDLE);

        Box savedBox = boxRepository.save(box);

        return toBoxResponse(savedBox);
    }

    @Transactional
    public List<ItemResponse> loadBox(UUID boxId, LoadItemsRequest request) {

        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new BoxNotFoundException ("Box not found"));

        validateBoxCanBeLoaded(box);

        int totalWeight = request.items()
                .stream()
                .mapToInt(ItemRequest::weight)
                .sum();

        if (totalWeight > box.getWeightLimit()) {
            throw new BoxLoadingException(
                    "Total item weight exceeds the box weight limit"
            );
        }

        box.setState(BoxState.LOADING);
        boxRepository.save(box);

        List<Item> items = request.items()
                .stream()
                .map(itemRequest -> {
                    Item item = new Item();
                    item.setName(itemRequest.name());
                    item.setWeight(itemRequest.weight());
                    item.setCode(itemRequest.code());
                    item.setBox(box);
                    return item;
                })
                .toList();

        List<Item> savedItems = itemRepository.saveAll(items);

        box.setState(BoxState.LOADED);
        boxRepository.save(box);

        return savedItems.stream()
                .map(this::toItemResponse)
                .toList();
    }

    public List<ItemResponse> getBoxItems(UUID boxId) {

        if (!boxRepository.existsById(boxId)) {
            throw new BoxNotFoundException("Box not found");
        }

        return itemRepository.findByBoxId(boxId)
                .stream()
                .map(this::toItemResponse)
                .toList();
    }

    public List<BoxResponse> getAvailableBoxes() {

        return boxRepository
                .findByStateAndBatteryLevelGreaterThanEqual(
                        BoxState.IDLE,
                        MINIMUM_LOADING_BATTERY
                )
                .stream()
                .map(this::toBoxResponse)
                .toList();
    }

    public BatteryResponse getBatteryLevel(UUID boxId) {

        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new BoxNotFoundException("Box not found"));

        return new BatteryResponse(
                box.getId(),
                box.getBatteryLevel()
        );
    }

    private void validateBoxCanBeLoaded(Box box) {

        if (box.getBatteryLevel() < MINIMUM_LOADING_BATTERY) {
            throw new BoxLoadingException(
                    "Box battery level must be at least 25% to load"
            );
        }

        if (box.getState() != BoxState.IDLE) {
            throw new BoxLoadingException(
                    "Box must be in IDLE state to be loaded"
            );
        }
    }

    private BoxResponse toBoxResponse(Box box) {

        return new BoxResponse(
                box.getId(),
                box.getTxref(),
                box.getWeightLimit(),
                box.getBatteryLevel(),
                box.getState()
        );
    }

    private ItemResponse toItemResponse(Item item) {

        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getWeight(),
                item.getCode()
        );
    }
}