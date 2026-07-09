package com.supplychain.dto;

import lombok.Data;

@Data
public class ShipmentDto {

    private Long id;

    private String shipmentNumber;

    private String customerName;

    private String origin;

    private String destination;
}