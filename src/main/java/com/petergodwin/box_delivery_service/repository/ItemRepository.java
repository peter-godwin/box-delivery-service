package com.petergodwin.box_delivery_service.repository;

import com.petergodwin.box_delivery_service.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    List<Item> findByBoxId(UUID boxId);

}