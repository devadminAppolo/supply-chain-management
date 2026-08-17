package com.supplychain.service;

import com.supplychain.dto.BinTransferDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.entity.Bin;
import com.supplychain.entity.BinTransfer;
import com.supplychain.entity.Inventory;
import com.supplychain.entity.InventoryTransaction;
import com.supplychain.entity.Sku;
import com.supplychain.entity.Uom;
import com.supplychain.entity.Warehouse;
import com.supplychain.enums.InventoryStatus;
import com.supplychain.enums.TransactionType;
import com.supplychain.mapper.BinTransferMapper;
import com.supplychain.repository.BinRepository;
import com.supplychain.repository.BinTransferRepository;
import com.supplychain.repository.InventoryRepository;
import com.supplychain.repository.InventoryTransactionRepository;
import com.supplychain.repository.SkuRepository;
import com.supplychain.repository.UomRepository;
import com.supplychain.repository.WarehouseRepository;
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

    // -------------------------------------------------------------------------
    // Transfer — the core atomic business operation
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public BinTransferDto transfer(BinTransferDto dto) {

        // ── 1. Resolve all names → Long IDs ──────────────────────────────────
        Long srcWarehouseId  = resolveWarehouseId(dto.getSourceWarehouseId());
        Long dstWarehouseId  = resolveWarehouseId(dto.getDestinationWarehouseId());
        Long srcBinId        = resolveBinId(dto.getSourceBinId(),       srcWarehouseId);
        Long dstBinId        = resolveBinId(dto.getDestinationBinId(),  dstWarehouseId);
        Long skuId           = resolveSkuId(dto.getSkuId());
        Long uomId           = resolveUomId(dto.getUomId());

        // ── 2. Business validations ───────────────────────────────────────────
        if (srcBinId.equals(dstBinId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Source and destination bins must be different");
        }

        // Same-warehouse constraint (relax here to enable cross-warehouse in future)
        if (!srcWarehouseId.equals(dstWarehouseId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cross-warehouse transfers are not yet supported; source and destination must be in the same warehouse");
        }

        // ── 3. Lock and validate source inventory ─────────────────────────────
        Inventory srcInventory = inventoryRepository
                .findInventoryByKey(srcWarehouseId, srcBinId, skuId, null, null, InventoryStatus.AVAILABLE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No AVAILABLE inventory found for the selected SKU in the source bin"));

        if (srcInventory.getAvailableQuantity().compareTo(dto.getQuantity()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Insufficient available quantity in source bin. Available: %s, Requested: %s",
                            srcInventory.getAvailableQuantity(), dto.getQuantity()));
        }

        // ── 4. Persist the BinTransfer header ─────────────────────────────────
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
        log.info("Bin transfer id={}: {} bin={} → bin={}, sku={}, qty={}",
                savedTransfer.getId(), dto.getSourceWarehouseId(),
                dto.getSourceBinId(), dto.getDestinationBinId(), dto.getSkuId(), dto.getQuantity());

        // ── 5. Deduct from source inventory ───────────────────────────────────
        BigDecimal quantityBefore = srcInventory.getOnHandQuantity();
        srcInventory.setOnHandQuantity(srcInventory.getOnHandQuantity().subtract(dto.getQuantity()));
        srcInventory.setAvailableQuantity(
                srcInventory.getOnHandQuantity()
                        .subtract(srcInventory.getReservedQuantity())
                        .subtract(srcInventory.getAllocatedQuantity()));
        inventoryRepository.save(srcInventory);
        log.info("Source inventory id={}: onHand {} → {}",
                srcInventory.getId(), quantityBefore, srcInventory.getOnHandQuantity());

        // ── 6. Increase destination inventory (find or create) ─────────────────
        // Lock destination row too so concurrent transfers to the same bin queue up
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
            // Derive productId from the SKU (Inventory requires it)
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

        // ── 7. Create audit transaction ────────────────────────────────────────
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

        // ── 8. Mark transfer completed ─────────────────────────────────────────
        savedTransfer.setStatus("COMPLETED");
        BinTransfer completed = repository.save(savedTransfer);
        log.info("Bin transfer id={} completed successfully", completed.getId());

        return enrich(mapper.toDto(completed), completed);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Delete (soft)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

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

    /** Resolves Long IDs → String names for the response DTO. */
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
