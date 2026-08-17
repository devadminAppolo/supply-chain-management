package com.supplychain.service;

import com.supplychain.dto.InventoryTransactionDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.entity.Inventory;
import com.supplychain.entity.InventoryTransaction;
import com.supplychain.entity.Sku;
import com.supplychain.entity.Warehouse;
import com.supplychain.enums.TransactionType;
import com.supplychain.mapper.InventoryTransactionMapper;
import com.supplychain.repository.BinRepository;
import com.supplychain.repository.InventoryRepository;
import com.supplychain.repository.InventoryTransactionRepository;
import com.supplychain.repository.ProductRepository;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    private final InventoryTransactionRepository repository;
    private final InventoryTransactionMapper     mapper;
    private final WarehouseRepository            warehouseRepository;
    private final InventoryRepository            inventoryRepository;
    private final ProductRepository              productRepository;
    private final SkuRepository                  skuRepository;
    private final UomRepository                  uomRepository;
    private final BinRepository                  binRepository;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public InventoryTransactionDto save(InventoryTransactionDto dto) {
        // Resolve names → IDs
        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        Long productId   = resolveProductId(dto.getProductId());
        Long skuId       = resolveSkuId(dto.getSkuId());
        Long uomId       = resolveUomId(dto.getUomId());
        Long fromBinId   = dto.getFromBinId() != null ? resolveBinId(dto.getFromBinId(), warehouseId) : null;
        Long toBinId     = dto.getToBinId()   != null ? resolveBinId(dto.getToBinId(),   warehouseId) : null;

        // Validate inventory record (still a Long ID, not a dropdown)
        Inventory inventory = inventoryRepository.findById(dto.getInventoryId())
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Inventory not found with id: " + dto.getInventoryId()));
        if (!inventory.getWarehouseId().equals(warehouseId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Inventory does not belong to the selected warehouse");
        }
        if (!inventory.getSkuId().equals(skuId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Inventory does not belong to the selected SKU");
        }

        InventoryTransaction entity = mapper.toEntity(dto);
        entity.setWarehouseId(warehouseId);
        entity.setProductId(productId);
        entity.setSkuId(skuId);
        entity.setUomId(uomId);
        entity.setFromBinId(fromBinId);
        entity.setToBinId(toBinId);
        entity.setDeleted(false);
        InventoryTransaction saved = repository.save(entity);
        log.info("Created inventory transaction id={}, type={}, inventoryId={}",
                saved.getId(), saved.getTransactionType(), saved.getInventoryId());
        return enrich(mapper.toDto(saved), saved);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public InventoryTransactionDto update(Long id, InventoryTransactionDto dto) {
        InventoryTransaction entity = findActiveOrThrow(id);

        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        Long productId   = resolveProductId(dto.getProductId());
        Long skuId       = resolveSkuId(dto.getSkuId());
        Long uomId       = resolveUomId(dto.getUomId());
        Long fromBinId   = dto.getFromBinId() != null ? resolveBinId(dto.getFromBinId(), warehouseId) : null;
        Long toBinId     = dto.getToBinId()   != null ? resolveBinId(dto.getToBinId(),   warehouseId) : null;

        mapper.updateEntity(dto, entity);
        entity.setWarehouseId(warehouseId);
        entity.setProductId(productId);
        entity.setSkuId(skuId);
        entity.setUomId(uomId);
        entity.setFromBinId(fromBinId);
        entity.setToBinId(toBinId);
        InventoryTransaction saved = repository.save(entity);
        log.info("Updated inventory transaction id={}", saved.getId());
        return enrich(mapper.toDto(saved), saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    public InventoryTransactionDto findById(Long id) {
        InventoryTransaction entity = findActiveOrThrow(id);
        return enrich(mapper.toDto(entity), entity);
    }

    @Override
    public PageResponse<InventoryTransactionDto> findAll(String search,
                                                         Long warehouseId,
                                                         Long inventoryId,
                                                         Long skuId,
                                                         TransactionType transactionType,
                                                         Pageable pageable) {
        Specification<InventoryTransaction> spec = buildSpec(search, warehouseId, inventoryId, skuId, transactionType);
        return PageResponse.of(repository.findAll(spec, pageable).map(e -> enrich(mapper.toDto(e), e)));
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteById(Long id) {
        InventoryTransaction entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted inventory transaction id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<InventoryTransaction> entities = repository.findAllById(ids).stream()
                .filter(t -> !t.isDeleted())
                .toList();
        entities.forEach(t -> t.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} inventory transactions", entities.size());
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private InventoryTransaction findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inventory transaction not found with id: " + id));
    }

    private Long resolveWarehouseId(String name) {
        return warehouseRepository.findByWarehouseNameAndDeletedFalse(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Warehouse not found with name: " + name))
                .getId();
    }

    private Long resolveProductId(String name) {
        return productRepository.findByProductNameAndDeletedFalse(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product not found with name: " + name))
                .getId();
    }

    private Long resolveSkuId(String skuCode) {
        return skuRepository.findBySkuCodeAndDeletedFalse(skuCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "SKU not found with code: " + skuCode))
                .getId();
    }

    private Long resolveUomId(String name) {
        return uomRepository.findByUomNameAndDeletedFalse(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "UOM not found with name: " + name))
                .getId();
    }

    private Long resolveBinId(String binCode, Long warehouseId) {
        return binRepository.findByBinCodeAndWarehouseIdAndDeletedFalse(binCode, warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Bin not found with code: " + binCode))
                .getId();
    }

    private InventoryTransactionDto enrich(InventoryTransactionDto dto, InventoryTransaction entity) {
        warehouseRepository.findById(entity.getWarehouseId())
                .ifPresent(w -> dto.setWarehouseId(w.getWarehouseName()));
        productRepository.findById(entity.getProductId())
                .ifPresent(p -> dto.setProductId(p.getProductName()));
        skuRepository.findById(entity.getSkuId())
                .ifPresent(s -> dto.setSkuId(s.getSkuCode()));
        uomRepository.findById(entity.getUomId())
                .ifPresent(u -> dto.setUomId(u.getUomName()));
        if (entity.getFromBinId() != null) {
            binRepository.findById(entity.getFromBinId())
                    .ifPresent(b -> dto.setFromBinId(b.getBinCode()));
        }
        if (entity.getToBinId() != null) {
            binRepository.findById(entity.getToBinId())
                    .ifPresent(b -> dto.setToBinId(b.getBinCode()));
        }
        return dto;
    }

    private Specification<InventoryTransaction> buildSpec(String search,
                                                          Long warehouseId,
                                                          Long inventoryId,
                                                          Long skuId,
                                                          TransactionType transactionType) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (warehouseId != null)     pred = cb.and(pred, cb.equal(root.get("warehouseId"), warehouseId));
            if (inventoryId != null)     pred = cb.and(pred, cb.equal(root.get("inventoryId"), inventoryId));
            if (skuId != null)           pred = cb.and(pred, cb.equal(root.get("skuId"), skuId));
            if (transactionType != null) pred = cb.and(pred, cb.equal(root.get("transactionType"), transactionType));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.or(
                        cb.like(cb.lower(root.get("lotNumber")),    pattern),
                        cb.like(cb.lower(root.get("serialNumber")), pattern),
                        cb.like(cb.lower(root.get("referenceId")),  pattern),
                        cb.like(cb.lower(root.get("reasonCode")),   pattern)
                ));
            }
            return pred;
        };
    }
}
