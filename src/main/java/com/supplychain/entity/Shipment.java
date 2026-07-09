package com.supplychain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_number")
    private String shipmentNumber;

    @Column(name = "customer_name")
    private String customerName;

    private String origin;

    private String destination;
}