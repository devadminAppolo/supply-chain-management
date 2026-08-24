package com.supplychain.wms.dashboard.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class InventoryByStatusDto {
    private final String status;
    private final BigDecimal quantity;
}
