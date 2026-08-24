package com.supplychain.wms.dashboard.application.service;

import com.supplychain.catalog.domain.model.Product;
import com.supplychain.catalog.domain.model.Sku;
import com.supplychain.catalog.infrastructure.repository.ProductRepository;
import com.supplychain.catalog.infrastructure.repository.SkuRepository;
import com.supplychain.wms.dashboard.application.dto.DashboardDto;
import com.supplychain.wms.dashboard.application.dto.InventoryByStatusDto;
import com.supplychain.wms.dashboard.application.dto.InventoryByWarehouseDto;
import com.supplychain.wms.dashboard.application.dto.InventoryKpiDto;
import com.supplychain.wms.dashboard.application.dto.RecentTransactionDto;
import com.supplychain.wms.inventory.infrastructure.repository.InventoryRepository;
import com.supplychain.wms.inventoryadjustment.infrastructure.repository.InventoryAdjustmentRepository;
import com.supplychain.wms.inventorytransaction.domain.model.InventoryTransaction;
import com.supplychain.wms.inventorytransaction.infrastructure.repository.InventoryTransactionRepository;
import com.supplychain.wms.location.domain.model.Bin;
import com.supplychain.wms.location.domain.model.Warehouse;
import com.supplychain.wms.location.infrastructure.repository.BinRepository;
import com.supplychain.wms.location.infrastructure.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_TRANSACTION_LIMIT = 10;

    private final InventoryRepository            inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryAdjustmentRepository  adjustmentRepository;
    private final WarehouseRepository            warehouseRepository;
    private final SkuRepository                  skuRepository;
    private final ProductRepository              productRepository;
    private final BinRepository                  binRepository;

    @Override
    public DashboardDto getInventorySummary() {
        return new DashboardDto(
                buildKpiSummary(),
                buildInventoryByWarehouse(),
                buildInventoryByStatus(),
                buildRecentTransactions()
        );
    }

    private InventoryKpiDto buildKpiSummary() {
        List<Object[]> rows = inventoryRepository.sumAllQuantities();
        BigDecimal totalOnHand    = BigDecimal.ZERO;
        BigDecimal totalAvailable = BigDecimal.ZERO;
        BigDecimal totalReserved  = BigDecimal.ZERO;
        BigDecimal totalAllocated = BigDecimal.ZERO;

        if (!rows.isEmpty()) {
            Object[] row = rows.get(0);
            totalOnHand    = toBigDecimal(row[0]);
            totalAvailable = toBigDecimal(row[1]);
            totalReserved  = toBigDecimal(row[2]);
            totalAllocated = toBigDecimal(row[3]);
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = startOfDay.plusDays(1);

        long todayTransactions = transactionRepository.countTodayTransactions(startOfDay, endOfDay);
        long todayAdjustments  = adjustmentRepository.countTodayAdjustments(startOfDay, endOfDay);

        return new InventoryKpiDto(
                totalOnHand, totalAvailable, totalReserved, totalAllocated,
                todayTransactions, todayAdjustments
        );
    }

    private List<InventoryByWarehouseDto> buildInventoryByWarehouse() {
        List<Object[]> rows = inventoryRepository.sumQuantitiesByWarehouse();
        if (rows.isEmpty()) return List.of();

        List<Long> warehouseIds = rows.stream()
                .map(r -> (Long) r[0])
                .toList();
        Map<Long, String> warehouseNames = warehouseRepository.findAllById(warehouseIds)
                .stream()
                .collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));

        return rows.stream()
                .map(row -> new InventoryByWarehouseDto(
                        warehouseNames.getOrDefault((Long) row[0], "Warehouse-" + row[0]),
                        toBigDecimal(row[1]),
                        toBigDecimal(row[2]),
                        toBigDecimal(row[3]),
                        toBigDecimal(row[4])
                ))
                .sorted(Comparator.comparing(InventoryByWarehouseDto::getWarehouse))
                .toList();
    }

    private List<InventoryByStatusDto> buildInventoryByStatus() {
        List<Object[]> rows = inventoryRepository.sumQuantitiesByStatus();
        if (rows.isEmpty()) return List.of();

        return rows.stream()
                .map(row -> new InventoryByStatusDto(
                        row[0].toString(),
                        toBigDecimal(row[1])
                ))
                .toList();
    }

    private List<RecentTransactionDto> buildRecentTransactions() {
        List<InventoryTransaction> txns = transactionRepository
                .findRecentTransactions(PageRequest.of(0, RECENT_TRANSACTION_LIMIT));
        if (txns.isEmpty()) return List.of();

        Set<Long> skuIds     = txns.stream().map(InventoryTransaction::getSkuId).collect(Collectors.toSet());
        Set<Long> productIds = txns.stream().map(InventoryTransaction::getProductId).collect(Collectors.toSet());
        Set<Long> binIds     = txns.stream()
                .flatMap(t -> Stream.of(t.getFromBinId(), t.getToBinId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> skuCodes     = skuRepository.findAllById(skuIds)
                .stream().collect(Collectors.toMap(Sku::getId, Sku::getSkuCode));
        Map<Long, String> productNames = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Product::getProductName));
        Map<Long, String> binCodes     = binRepository.findAllById(binIds)
                .stream().collect(Collectors.toMap(Bin::getId, Bin::getBinCode));

        return txns.stream()
                .map(t -> new RecentTransactionDto(
                        t.getId(),
                        t.getTransactionType().name(),
                        skuCodes.get(t.getSkuId()),
                        productNames.get(t.getProductId()),
                        t.getQuantity(),
                        t.getFromBinId() != null ? binCodes.get(t.getFromBinId()) : null,
                        t.getToBinId()   != null ? binCodes.get(t.getToBinId())   : null,
                        t.getTransactionDate()
                ))
                .toList();
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }
}
