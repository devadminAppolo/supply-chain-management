package com.supplychain.wms.inventoryadjustment.application.service;

import com.supplychain.catalog.domain.model.Sku;
import com.supplychain.catalog.infrastructure.repository.SkuRepository;
import com.supplychain.catalog.infrastructure.repository.UomRepository;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.AdjustmentType;
import com.supplychain.common.domain.enums.InventoryStatus;
import com.supplychain.common.domain.enums.TransactionType;
import com.supplychain.wms.inventory.domain.model.Inventory;
import com.supplychain.wms.inventory.infrastructure.repository.InventoryRepository;
import com.supplychain.wms.inventoryadjustment.application.dto.InventoryAdjustmentDto;
import com.supplychain.wms.inventoryadjustment.domain.model.InventoryAdjustment;
import com.supplychain.wms.inventoryadjustment.infrastructure.mapper.InventoryAdjustmentMapper;
import com.supplychain.wms.inventoryadjustment.infrastructure.repository.InventoryAdjustmentRepository;
import com.supplychain.wms.inventorytransaction.domain.model.InventoryTransaction;
import com.supplychain.wms.inventorytransaction.infrastructure.repository.InventoryTransactionRepository;
import com.supplychain.wms.location.infrastructure.repository.BinRepository;
import com.supplychain.wms.location.infrastructure.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryAdjustmentServiceImpl implements InventoryAdjustmentService {

    private final InventoryAdjustmentRepository    repository;
    private final InventoryAdjustmentMapper        mapper;
    private final WarehouseRepository              warehouseRepository;
    private final SkuRepository                    skuRepository;
    private final BinRepository                    binRepository;
    private final UomRepository                    uomRepository;
    private final InventoryRepository              inventoryRepository;
    private final InventoryTransactionRepository   inventoryTransactionRepository;

    @Override
    @Transactional
    public InventoryAdjustmentDto save(InventoryAdjustmentDto dto) {
        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        Long binId       = resolveBinId(dto.getBinId(), warehouseId);
        Long skuId       = resolveSkuId(dto.getSkuId());
        Long uomId       = resolveUomId(dto.getUomId());

        InventoryAdjustment entity = mapper.toEntity(dto);
        entity.setWarehouseId(warehouseId);
        entity.setBinId(binId);
        entity.setSkuId(skuId);
        entity.setUomId(uomId);
        entity.setDeleted(false);
        entity.setAdjustmentDate(LocalDateTime.now());
        entity.setStatus("PROCESSING");
        InventoryAdjustment saved = repository.save(entity);
        log.info("Saved adjustment id={}, type={}, skuId={}, binId={}",
                saved.getId(), saved.getAdjustmentType(), saved.getSkuId(), saved.getBinId());

        if (saved.getAdjustmentType() == AdjustmentType.ADJUSTMENT_IN) {
            applyAdjustmentIn(saved);
        }

        saved.setStatus("COMPLETED");
        return enrich(mapper.toDto(repository.save(saved)), saved);
    }

    @Override
    @Transactional
    public InventoryAdjustmentDto update(Long id, InventoryAdjustmentDto dto) {
        InventoryAdjustment entity = findActiveOrThrow(id);

        if ("COMPLETED".equals(entity.getStatus()) || "PROCESSING".equals(entity.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Inventory adjustment id=" + id + " has already been processed and cannot be modified");
        }

        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        Long binId       = resolveBinId(dto.getBinId(), warehouseId);
        Long skuId       = resolveSkuId(dto.getSkuId());
        Long uomId       = resolveUomId(dto.getUomId());

        mapper.updateEntity(dto, entity);
        entity.setWarehouseId(warehouseId);
        entity.setBinId(binId);
        entity.setSkuId(skuId);
        entity.setUomId(uomId);
        InventoryAdjustment saved = repository.save(entity);
        log.info("Updated inventory adjustment id={}", saved.getId());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    public InventoryAdjustmentDto findById(Long id) {
        InventoryAdjustment entity = findActiveOrThrow(id);
        return enrich(mapper.toDto(entity), entity);
    }

    @Override
    public PageResponse<InventoryAdjustmentDto> findAll(String search,
                                                         Long warehouseId,
                                                         Long skuId,
                                                         Long binId,
                                                         AdjustmentType adjustmentType,
                                                         String status,
                                                         Pageable pageable) {
        Specification<InventoryAdjustment> spec = buildSpec(search, warehouseId, skuId, binId, adjustmentType, status);
        return PageResponse.of(repository.findAll(spec, pageable).map(e -> enrich(mapper.toDto(e), e)));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        InventoryAdjustment entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted inventory adjustment id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<InventoryAdjustment> entities = repository.findAllById(ids).stream()
                .filter(a -> !a.isDeleted())
                .toList();
        entities.forEach(a -> a.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} inventory adjustments", entities.size());
    }

    private void applyAdjustmentIn(InventoryAdjustment adj) {
        Long productId = skuRepository.findById(adj.getSkuId())
                .filter(s -> !s.isDeleted())
                .map(Sku::getProductId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "SKU not found with id: " + adj.getSkuId()));

        Optional<Inventory> existing = inventoryRepository.findInventoryByKey(
                adj.getWarehouseId(), adj.getBinId(), adj.getSkuId(),
                null, null, InventoryStatus.AVAILABLE);

        final BigDecimal quantityBefore;
        final Inventory inventory;

        if (existing.isPresent()) {
            inventory      = existing.get();
            quantityBefore = inventory.getOnHandQuantity();
            inventory.setOnHandQuantity(inventory.getOnHandQuantity().add(adj.getQuantity()));
            inventory.setAvailableQuantity(
                    inventory.getOnHandQuantity()
                            .subtract(inventory.getReservedQuantity())
                            .subtract(inventory.getAllocatedQuantity()));
            log.info("ADJUSTMENT_IN: inventory id={} onHand {} -> {}",
                    inventory.getId(), quantityBefore, inventory.getOnHandQuantity());
        } else {
            quantityBefore = BigDecimal.ZERO;
            inventory = Inventory.builder()
                    .orgId(adj.getOrgId())
                    .dcId(adj.getDcId())
                    .warehouseId(adj.getWarehouseId())
                    .binId(adj.getBinId())
                    .productId(productId)
                    .skuId(adj.getSkuId())
                    .uomId(adj.getUomId())
                    .inventoryStatus(InventoryStatus.AVAILABLE)
                    .onHandQuantity(adj.getQuantity())
                    .reservedQuantity(BigDecimal.ZERO)
                    .allocatedQuantity(BigDecimal.ZERO)
                    .availableQuantity(adj.getQuantity())
                    .deleted(false)
                    .build();
            log.info("ADJUSTMENT_IN: creating new inventory skuId={}, binId={}, quantity={}",
                    adj.getSkuId(), adj.getBinId(), adj.getQuantity());
        }

        Inventory savedInventory = inventoryRepository.save(inventory);

        InventoryTransaction txn = InventoryTransaction.builder()
                .orgId(adj.getOrgId())
                .dcId(adj.getDcId())
                .warehouseId(adj.getWarehouseId())
                .inventoryId(savedInventory.getId())
                .productId(productId)
                .skuId(adj.getSkuId())
                .uomId(adj.getUomId())
                .fromBinId(null)
                .toBinId(adj.getBinId())
                .transactionType(TransactionType.ADJUSTMENT_IN)
                .quantity(adj.getQuantity())
                .quantityBefore(quantityBefore)
                .quantityAfter(savedInventory.getOnHandQuantity())
                .reasonCode(adj.getReasonCode())
                .referenceType("INVENTORY_ADJUSTMENT")
                .referenceId(adj.getId().toString())
                .remarks(adj.getRemarks())
                .transactionDate(adj.getAdjustmentDate())
                .deleted(false)
                .build();

        inventoryTransactionRepository.save(txn);
        log.info("ADJUSTMENT_IN complete: adjustment id={}, inventory id={}, before={}, qty={}, after={}",
                adj.getId(), savedInventory.getId(),
                quantityBefore, adj.getQuantity(), savedInventory.getOnHandQuantity());
    }

    private InventoryAdjustment findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inventory adjustment not found with id: " + id));
    }

    private Long resolveWarehouseId(String warehouseName) {
        return warehouseRepository.findByWarehouseNameAndDeletedFalse(warehouseName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Warehouse not found with name: " + warehouseName))
                .getId();
    }

    private Long resolveBinId(String binCode, Long warehouseId) {
        return binRepository.findByBinCodeAndWarehouseIdAndDeletedFalse(binCode, warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Bin not found with code: " + binCode))
                .getId();
    }

    private Long resolveSkuId(String skuCode) {
        return skuRepository.findBySkuCodeAndDeletedFalse(skuCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "SKU not found with code: " + skuCode))
                .getId();
    }

    private Long resolveUomId(String uomName) {
        return uomRepository.findByUomNameAndDeletedFalse(uomName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "UOM not found with name: " + uomName))
                .getId();
    }

    private InventoryAdjustmentDto enrich(InventoryAdjustmentDto dto, InventoryAdjustment entity) {
        warehouseRepository.findById(entity.getWarehouseId())
                .ifPresent(w -> dto.setWarehouseId(w.getWarehouseName()));
        binRepository.findById(entity.getBinId())
                .ifPresent(b -> dto.setBinId(b.getBinCode()));
        skuRepository.findById(entity.getSkuId())
                .ifPresent(s -> dto.setSkuId(s.getSkuCode()));
        uomRepository.findById(entity.getUomId())
                .ifPresent(u -> dto.setUomId(u.getUomName()));
        return dto;
    }

    private Specification<InventoryAdjustment> buildSpec(String search,
                                                          Long warehouseId,
                                                          Long skuId,
                                                          Long binId,
                                                          AdjustmentType adjustmentType,
                                                          String status) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (warehouseId != null)    pred = cb.and(pred, cb.equal(root.get("warehouseId"), warehouseId));
            if (skuId != null)          pred = cb.and(pred, cb.equal(root.get("skuId"), skuId));
            if (binId != null)          pred = cb.and(pred, cb.equal(root.get("binId"), binId));
            if (adjustmentType != null) pred = cb.and(pred, cb.equal(root.get("adjustmentType"), adjustmentType));
            if (status != null && !status.isBlank()) pred = cb.and(pred, cb.equal(root.get("status"), status));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.or(
                        cb.like(cb.lower(root.get("reasonCode")),      pattern),
                        cb.like(cb.lower(root.get("referenceNumber")), pattern)
                ));
            }
            return pred;
        };
    }
}
