package com.supplychain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * KPI summary card values for the WMS inventory dashboard.
 */
@Getter
@AllArgsConstructor
public class InventoryKpiDto {
    private final BigDecimal totalOnHand;
    private final BigDecimal totalAvailable;
    private final BigDecimal totalReserved;
    private final BigDecimal totalAllocated;
    private final long todayTransactions;
    private final long todayAdjustments;
}
