package com.supplychain.wms.dashboard.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

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
