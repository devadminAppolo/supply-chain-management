package com.supplychain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Inventory on-hand quantity grouped by InventoryStatus for the dashboard.
 */
@Getter
@AllArgsConstructor
public class InventoryByStatusDto {
    private final String status;
    private final BigDecimal quantity;
}
