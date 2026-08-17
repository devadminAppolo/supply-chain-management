package com.supplychain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Consolidated response for GET /api/dashboard/inventory-summary.
 * Contains all data required by the WMS Week 3 Dashboard in a single response.
 */
@Getter
@AllArgsConstructor
public class DashboardDto {
    private final InventoryKpiDto           summary;
    private final List<InventoryByWarehouseDto> inventoryByWarehouse;
    private final List<InventoryByStatusDto>    inventoryByStatus;
    private final List<RecentTransactionDto>    recentTransactions;
}
