package com.petergodwin.box_delivery_service.repository;

import com.petergodwin.box_delivery_service.entity.Box;
import com.petergodwin.box_delivery_service.entity.BoxState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BoxRepository extends JpaRepository<Box, UUID> {

    boolean existsByTxref(String txref);

    List<Box> findByStateAndBatteryLevelGreaterThanEqual(
            BoxState state,
            Integer batteryLevel
    );
}