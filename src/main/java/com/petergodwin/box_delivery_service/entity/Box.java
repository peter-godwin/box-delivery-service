package com.petergodwin.box_delivery_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "boxes")
@Getter
@Setter
@NoArgsConstructor
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String txref;

    @Column(nullable = false)
    private Integer weightLimit;

    @Column(nullable = false)
    private Integer batteryLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoxState state = BoxState.IDLE;
}