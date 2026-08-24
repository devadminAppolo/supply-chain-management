package com.supplychain.wms.bintransfer.application.service;

import com.supplychain.catalog.domain.model.Sku;
import com.supplychain.catalog.infrastructure.repository.SkuRepository;
import com.supplychain.catalog.infrastructure.repository.UomRepository;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.InventoryStatus;
import com.supplychain.common.domain.enums.TransactionType;
import com.supplychain.wms.bintransfer.application.dto.BinTransferDto;
import com.supplychain.wms.bintransfer.domain.model.BinTransfer;
import com.supplychain.wms.bintransfer.infrastructure.mapper.BinTransferMapper;
import com.supplychain.wms.bintransfer.infrastructure.repository.BinTransferRepository;
import com.supplychain.wms.inventory.domain.model.Inventory;
import com.supplychain.wms.inventory.infrastructure.repository.InventoryRepository;
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
public class BinTransferServiceImpl implements BinTransferService {

    private final BinTransferRepository          repository;
    private final BinTransferMapper              mapper;
    private final WarehouseRepository            warehouseRepository;
    private final BinRepository                  binRepository;
    private final SkuRepository                  skuRepository;
    private final UomRepository                  uomRepository;
    private final InventoryRepository            inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    @Transactional
    public BinTransferDto transfer(BinTransferDto dto) {
        Long srcWarehouseId  = resolveWarehouseId(dto.getSourceWarehouseId());
        Long dstWarehouseId  = resolveWarehouseId(dto.getDestinationWarehouseId());
        Long srcBinId        = resolveBinId(dto.getSourceBinId(),       srcWarehouseId);
        Long dstBinId        = resolveBinId(dto.getDestinationBinId(),  dstWarehouseId);
        Long skuId           = resolveSkuId(dto.getSkuId());
        Long uomId           = resolveUomId(dto.getUomId());

        if (srcBinId.equals(dstBinId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Source and destination bins must be different");
        }

        if (!srcWarehouseId.equals(dstWarehouseId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cross-warehouse transfers are not yet supported; source and destination must be in the same warehouse");
        }

        Inventory srcInventory = inventoryRepository
                .findInventoryByKey(srcWarehouseId, srcBinId, skuId, null, null, InventoryStatus.AVAILABLE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No AVAILABLE inventory found for the selected SKU in the source bin"));

        if (srcInventory.getAvailableQuantity().compareTo(dto.getQuantity()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Insufficient available quantity in source bin. Available: %s, Requested: %s",
                            srcInventory.getAvailableQuantity(), dto.getQuantity()));
        }

        BinTransfer transfer = mapper.toEntity(dto);
        transfer.setSourceWarehouseId(srcWarehouseId);
        transfer.setSourceBinId(srcBinId);
        transfer.setDestinationWarehouseId(dstWarehouseId);
        transfer.setDestinationBinId(dstBinId);
        transfer.setSkuId(skuId);
        transfer.setUomId(uomId);
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setStatus("PROCESSING");
        transfer.setDeleted(false);
        BinTransfer savedTransfer = repository.save(transfer);
        log.info("Bin transfer id={}: {} bin={} -> bin={}, sku={}, qty={}",
                savedTransfer.getId(), dto.getSourceWarehouseId(),
                dto.getSourceBinId(), dto.getDestinationBinId(), dto.getSkuId(), dto.getQuantity());

        BigDecimal quantityBefore = srcInventory.getOnHandQuantity();
        srcInventory.setOnHandQuantity(srcInventory.getOnHandQuantity().subtract(dto.getQuantity()));
        srcInventory.setAvailableQuantity(
                srcInventory.getOnHandQuantity()
                        .subtract(srcInventory.getReservedQuantity())
                        .subtract(srcInventory.getAllocatedQuantity()));
        inventoryRepository.save(srcInventory);
        log.info("Source inventory id={}: onHand {} -> {}",
                srcInventory.getId(), quantityBefore, srcInventory.getOnHandQuantity());

        Optional<Inventory> existingDst = inventoryRepository
                .findInventoryByKey(dstWarehouseId, dstBinId, skuId, null, null, InventoryStatus.AVAILABLE);

        Long dstInventoryId;
        if (existingDst.isPresent()) {
            Inventory dstInventory = existingDst.get();
            dstInventory.setOnHandQuantity(dstInventory.getOnHandQuantity().add(dto.getQuantity()));
            dstInventory.setAvailableQuantity(
                    dstInventory.getOnHandQuantity()
                            .subtract(dstInventory.getReservedQuantity())
                            .subtract(dstInventory.getAllocatedQuantity()));
            dstInventoryId = inventoryRepository.save(dstInventory).getId();
            log.info("Destination inventory id={}: onHand incremented by {}", dstInventoryId, dto.getQuantity());
        } else {
            Long productId = skuRepository.findById(skuId)
                    .map(Sku::getProductId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "SKU not found while creating destination inventory"));

            Inventory newDst = Inventory.builder()
                    .orgId(srcInventory.getOrgId())
                    .dcId(srcInventory.getDcId())
                    .warehouseId(dstWarehouseId)
                    .binId(dstBinId)
                    .productId(productId)
                    .skuId(skuId)
                    .uomId(uomId)
                    .inventoryStatus(InventoryStatus.AVAILABLE)
                    .onHandQuantity(dto.getQuantity())
                    .reservedQuantity(BigDecimal.ZERO)
                    .allocatedQuantity(BigDecimal.ZERO)
                    .availableQuantity(dto.getQuantity())
                    .deleted(false)
                    .build();
            dstInventoryId = inventoryRepository.save(newDst).getId();
            log.info("Created destination inventory id={} in bin={}", dstInventoryId, dstBinId);
        }

        InventoryTransaction txn = InventoryTransaction.builder()
                .orgId(savedTransfer.getOrgId())
                .dcId(savedTransfer.getDcId())
                .warehouseId(srcWarehouseId)
                .inventoryId(srcInventory.getId())
                .productId(srcInventory.getProductId())
                .skuId(skuId)
                .uomId(uomId)
                .fromBinId(srcBinId)
                .toBinId(dstBinId)
                .transactionType(TransactionType.TRANSFER)
                .quantity(dto.getQuantity())
                .quantityBefore(quantityBefore)
                .quantityAfter(srcInventory.getOnHandQuantity())
                .reasonCode(dto.getReasonCode())
                .referenceType("BIN_TRANSFER")
                .referenceId(savedTransfer.getId().toString())
                .remarks(dto.getRemarks())
                .transactionDate(savedTransfer.getTransferDate())
                .deleted(false)
                .build();
        inventoryTransactionRepository.save(txn);

        savedTransfer.setStatus("COMPLETED");
        BinTransfer completed = repository.save(savedTransfer);
        log.info("Bin transfer id={} completed successfully", completed.getId());

        return enrich(mapper.toDto(completed), completed);
    }

    @Override
    public BinTransferDto findById(Long id) {
        BinTransfer entity = findActiveOrThrow(id);
        return enrich(mapper.toDto(entity), entity);
    }

    @Override
    public PageResponse<BinTransferDto> findAll(String search,
                                                Long sourceWarehouseId,
                                                Long sourceBinId,
                                                Long skuId,
                                                Pageable pageable) {
        Specification<BinTransfer> spec = buildSpec(search, sourceWarehouseId, sourceBinId, skuId);
        return PageResponse.of(repository.findAll(spec, pageable).map(e -> enrich(mapper.toDto(e), e)));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        BinTransfer entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted bin transfer id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<BinTransfer> entities = repository.findAllById(ids).stream()
                .filter(t -> !t.isDeleted())
                .toList();
        entities.forEach(t -> t.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} bin transfers", entities.size());
    }

    private BinTransfer findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Bin transfer not found with id: " + id));
    }

    private Long resolveWarehouseId(String name) {
        return warehouseRepository.findByWarehouseNameAndDeletedFalse(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Warehouse not found with name: " + name))
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

    private BinTransferDto enrich(BinTransferDto dto, BinTransfer entity) {
        warehouseRepository.findById(entity.getSourceWarehouseId())
                .ifPresent(w -> dto.setSourceWarehouseId(w.getWarehouseName()));
        warehouseRepository.findById(entity.getDestinationWarehouseId())
                .ifPresent(w -> dto.setDestinationWarehouseId(w.getWarehouseName()));
        binRepository.findById(entity.getSourceBinId())
                .ifPresent(b -> dto.setSourceBinId(b.getBinCode()));
        binRepository.findById(entity.getDestinationBinId())
                .ifPresent(b -> dto.setDestinationBinId(b.getBinCode()));
        skuRepository.findById(entity.getSkuId())
                .ifPresent(s -> dto.setSkuId(s.getSkuCode()));
        uomRepository.findById(entity.getUomId())
                .ifPresent(u -> dto.setUomId(u.getUomName()));
        return dto;
    }

    private Specification<BinTransfer> buildSpec(String search,
                                                  Long sourceWarehouseId,
                                                  Long sourceBinId,
                                                  Long skuId) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (sourceWarehouseId != null)
                pred = cb.and(pred, cb.equal(root.get("sourceWarehouseId"), sourceWarehouseId));
            if (sourceBinId != null)
                pred = cb.and(pred, cb.equal(root.get("sourceBinId"), sourceBinId));
            if (skuId != null)
                pred = cb.and(pred, cb.equal(root.get("skuId"), skuId));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.like(cb.lower(root.get("reasonCode")), pattern));
            }
            return pred;
        };
    }
}
