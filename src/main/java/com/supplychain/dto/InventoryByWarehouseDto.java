package com.supplychain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Per-warehouse inventory quantity summary for the dashboard.
 */
@Getter
@AllArgsConstructor
public class InventoryByWarehouseDto {
    private final String warehouse;
    private final BigDecimal totalOnHand;
    private final BigDecimal totalAvailable;
    private final BigDecimal totalReserved;
    private final BigDecimal totalAllocated;
}
