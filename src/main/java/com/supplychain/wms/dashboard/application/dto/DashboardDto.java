package com.supplychain.wms.dashboard.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardDto {
    private final InventoryKpiDto               summary;
    private final List<InventoryByWarehouseDto> inventoryByWarehouse;
    private final List<InventoryByStatusDto>    inventoryByStatus;
    private final List<RecentTransactionDto>    recentTransactions;
}
