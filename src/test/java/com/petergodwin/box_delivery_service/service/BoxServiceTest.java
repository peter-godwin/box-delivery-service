package com.petergodwin.box_delivery_service.service;

import com.petergodwin.box_delivery_service.dto.*;
import com.petergodwin.box_delivery_service.entity.Box;
import com.petergodwin.box_delivery_service.entity.BoxState;
import com.petergodwin.box_delivery_service.entity.Item;
import com.petergodwin.box_delivery_service.exception.BoxLoadingException;
import com.petergodwin.box_delivery_service.exception.BoxNotFoundException;
import com.petergodwin.box_delivery_service.repository.BoxRepository;
import com.petergodwin.box_delivery_service.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoxServiceTest {

    @Mock
    private BoxRepository boxRepository;

    @Mock
    private ItemRepository itemRepository;

    private BoxService boxService;

    @BeforeEach
    void setUp() {
        boxService = new BoxService(boxRepository, itemRepository);
    }

    @Test
    void shouldCreateBox() {
        CreateBoxRequest request = new CreateBoxRequest(
                "BOX-001",
                500,
                80
        );

        Box savedBox = new Box();
        savedBox.setId(UUID.randomUUID());
        savedBox.setTxref("BOX-001");
        savedBox.setWeightLimit(500);
        savedBox.setBatteryLevel(80);
        savedBox.setState(BoxState.IDLE);

        when(boxRepository.existsByTxref("BOX-001")).thenReturn(false);
        when(boxRepository.save(any(Box.class))).thenReturn(savedBox);

        BoxResponse response = boxService.createBox(request);

        assertEquals("BOX-001", response.txref());
        assertEquals(500, response.weightLimit());
        assertEquals(80, response.batteryLevel());
        assertEquals(BoxState.IDLE, response.state());

        verify(boxRepository).save(any(Box.class));
    }

    @Test
    void shouldRejectDuplicateTxref() {
        CreateBoxRequest request = new CreateBoxRequest(
                "BOX-001",
                500,
                80
        );

        when(boxRepository.existsByTxref("BOX-001")).thenReturn(true);

        assertThrows(
                BoxLoadingException.class,
                () -> boxService.createBox(request)
        );

        verify(boxRepository, never()).save(any(Box.class));
    }

    @Test
    void shouldLoadBoxSuccessfully() {
        UUID boxId = UUID.randomUUID();

        Box box = createBox(
                boxId,
                "BOX-001",
                500,
                80,
                BoxState.IDLE
        );

        LoadItemsRequest request = new LoadItemsRequest(
                List.of(
                        new ItemRequest("Medicine-01", 200, "MED_001"),
                        new ItemRequest("Food_02", 100, "FOOD_002")
                )
        );

        Item item1 = createItem(
                UUID.randomUUID(),
                "Medicine-01",
                200,
                "MED_001",
                box
        );

        Item item2 = createItem(
                UUID.randomUUID(),
                "Food_02",
                100,
                "FOOD_002",
                box
        );

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(itemRepository.saveAll(anyList()))
                .thenReturn(List.of(item1, item2));

        List<ItemResponse> response = boxService.loadBox(boxId, request);

        assertEquals(2, response.size());
        assertEquals("Medicine-01", response.get(0).name());
        assertEquals("Food_02", response.get(1).name());

        assertEquals(BoxState.LOADED, box.getState());

        verify(itemRepository).saveAll(anyList());
        verify(boxRepository, times(2)).save(box);
    }

    @Test
    void shouldRejectLoadingWhenWeightExceedsLimit() {
        UUID boxId = UUID.randomUUID();

        Box box = createBox(
                boxId,
                "BOX-001",
                500,
                80,
                BoxState.IDLE
        );

        LoadItemsRequest request = new LoadItemsRequest(
                List.of(
                        new ItemRequest("Item-01", 300, "ITEM_001"),
                        new ItemRequest("Item-02", 250, "ITEM_002")
                )
        );

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));

        BoxLoadingException exception = assertThrows(
                BoxLoadingException.class,
                () -> boxService.loadBox(boxId, request)
        );

        assertEquals(
                "Total item weight exceeds the box weight limit",
                exception.getMessage()
        );

        verify(itemRepository, never()).saveAll(anyList());
        assertEquals(BoxState.IDLE, box.getState());
    }

    @Test
    void shouldRejectLoadingWhenBatteryIsBelow25Percent() {
        UUID boxId = UUID.randomUUID();

        Box box = createBox(
                boxId,
                "BOX-003",
                500,
                24,
                BoxState.IDLE
        );

        LoadItemsRequest request = new LoadItemsRequest(
                List.of(
                        new ItemRequest("Item-01", 100, "ITEM_001")
                )
        );

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));

        BoxLoadingException exception = assertThrows(
                BoxLoadingException.class,
                () -> boxService.loadBox(boxId, request)
        );

        assertEquals(
                "Box battery level must be at least 25% to load",
                exception.getMessage()
        );

        verify(itemRepository, never()).saveAll(anyList());
        assertEquals(BoxState.IDLE, box.getState());
    }

    @Test
    void shouldRejectLoadingWhenBoxIsNotIdle() {
        UUID boxId = UUID.randomUUID();

        Box box = createBox(
                boxId,
                "BOX-001",
                500,
                80,
                BoxState.LOADED
        );

        LoadItemsRequest request = new LoadItemsRequest(
                List.of(
                        new ItemRequest("Item-01", 100, "ITEM_001")
                )
        );

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));

        BoxLoadingException exception = assertThrows(
                BoxLoadingException.class,
                () -> boxService.loadBox(boxId, request)
        );

        assertEquals(
                "Box must be in IDLE state to be loaded",
                exception.getMessage()
        );

        verify(itemRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldRejectLoadingWhenBoxDoesNotExist() {
        UUID boxId = UUID.randomUUID();

        LoadItemsRequest request = new LoadItemsRequest(
                List.of(
                        new ItemRequest("Item-01", 100, "ITEM_001")
                )
        );

        when(boxRepository.findById(boxId)).thenReturn(Optional.empty());

        assertThrows(
                BoxNotFoundException.class,
                () -> boxService.loadBox(boxId, request)
        );

        verify(itemRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldReturnLoadedItems() {
        UUID boxId = UUID.randomUUID();

        Item item = createItem(
                UUID.randomUUID(),
                "Medicine-01",
                200,
                "MED_001",
                null
        );

        when(boxRepository.existsById(boxId)).thenReturn(true);
        when(itemRepository.findByBoxId(boxId))
                .thenReturn(List.of(item));

        List<ItemResponse> response = boxService.getBoxItems(boxId);

        assertEquals(1, response.size());
        assertEquals("Medicine-01", response.get(0).name());
        assertEquals(200, response.get(0).weight());
        assertEquals("MED_001", response.get(0).code());
    }

    @Test
    void shouldReturnAvailableBoxes() {
        Box box = createBox(
                UUID.randomUUID(),
                "BOX-001",
                500,
                80,
                BoxState.IDLE
        );

        when(boxRepository.findByStateAndBatteryLevelGreaterThanEqual(
                BoxState.IDLE,
                25
        )).thenReturn(List.of(box));

        List<BoxResponse> response = boxService.getAvailableBoxes();

        assertEquals(1, response.size());
        assertEquals("BOX-001", response.get(0).txref());
        assertEquals(80, response.get(0).batteryLevel());
        assertEquals(BoxState.IDLE, response.get(0).state());
    }

    @Test
    void shouldReturnBatteryLevel() {
        UUID boxId = UUID.randomUUID();

        Box box = createBox(
                boxId,
                "BOX-001",
                500,
                80,
                BoxState.IDLE
        );

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));

        BatteryResponse response = boxService.getBatteryLevel(boxId);

        assertEquals(boxId, response.boxId());
        assertEquals(80, response.batteryLevel());
    }

    @Test
    void shouldRejectBatteryRequestWhenBoxDoesNotExist() {
        UUID boxId = UUID.randomUUID();

        when(boxRepository.findById(boxId)).thenReturn(Optional.empty());

        assertThrows(
                BoxNotFoundException.class,
                () -> boxService.getBatteryLevel(boxId)
        );
    }

    private Box createBox(
            UUID id,
            String txref,
            int weightLimit,
            int batteryLevel,
            BoxState state
    ) {
        Box box = new Box();
        box.setId(id);
        box.setTxref(txref);
        box.setWeightLimit(weightLimit);
        box.setBatteryLevel(batteryLevel);
        box.setState(state);
        return box;
    }

    private Item createItem(
            UUID id,
            String name,
            int weight,
            String code,
            Box box
    ) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setWeight(weight);
        item.setCode(code);
        item.setBox(box);
        return item;
    }
}