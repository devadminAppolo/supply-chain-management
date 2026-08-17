package com.supplychain.service;

import com.supplychain.dto.InventoryDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.entity.Bin;
import com.supplychain.entity.Inventory;
import com.supplychain.entity.Product;
import com.supplychain.entity.Sku;
import com.supplychain.entity.Uom;
import com.supplychain.entity.Warehouse;
import com.supplychain.enums.InventoryStatus;
import com.supplychain.mapper.InventoryMapper;
import com.supplychain.repository.BinRepository;
import com.supplychain.repository.InventoryRepository;
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

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository  repository;
    private final InventoryMapper      mapper;
    private final WarehouseRepository  warehouseRepository;
    private final BinRepository        binRepository;
    private final ProductRepository    productRepository;
    private final SkuRepository        skuRepository;
    private final UomRepository        uomRepository;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public InventoryDto save(InventoryDto dto) {
        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        Long binId       = resolveBinId(dto.getBinId(), warehouseId);
        Long productId   = resolveProductId(dto.getProductId());
        Long skuId       = resolveSkuId(dto.getSkuId(), productId);
        Long uomId       = resolveUomId(dto.getUomId());

        validateLotSerial(dto, productId);
        validateQuantities(dto);
        checkDuplicateOnCreate(warehouseId, binId, skuId, dto.getLotNumber(),
                dto.getSerialNumber(), dto.getInventoryStatus());

        Inventory entity = mapper.toEntity(dto);
        entity.setWarehouseId(warehouseId);
        entity.setBinId(binId);
        entity.setProductId(productId);
        entity.setSkuId(skuId);
        entity.setUomId(uomId);
        entity.setDeleted(false);
        Inventory saved = repository.save(entity);
        log.info("Created inventory id={}, skuId={}, binId={}", saved.getId(), saved.getSkuId(), saved.getBinId());
        return enrich(mapper.toDto(saved), saved);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public InventoryDto update(Long id, InventoryDto dto) {
        Inventory entity = findActiveOrThrow(id);
        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        Long binId       = resolveBinId(dto.getBinId(), warehouseId);
        Long productId   = resolveProductId(dto.getProductId());
        Long skuId       = resolveSkuId(dto.getSkuId(), productId);
        Long uomId       = resolveUomId(dto.getUomId());

        validateLotSerial(dto, productId);
        validateQuantities(dto);
        checkDuplicateOnUpdate(warehouseId, binId, skuId, dto.getLotNumber(),
                dto.getSerialNumber(), dto.getInventoryStatus(), id);

        mapper.updateEntity(dto, entity);
        entity.setWarehouseId(warehouseId);
        entity.setBinId(binId);
        entity.setProductId(productId);
        entity.setSkuId(skuId);
        entity.setUomId(uomId);
        Inventory saved = repository.save(entity);
        log.info("Updated inventory id={}", saved.getId());
        return enrich(mapper.toDto(saved), saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    public InventoryDto findById(Long id) {
        Inventory entity = findActiveOrThrow(id);
        return enrich(mapper.toDto(entity), entity);
    }

    @Override
    public PageResponse<InventoryDto> findAll(String search,
                                              Long warehouseId,
                                              Long binId,
                                              Long productId,
                                              Long skuId,
                                              InventoryStatus inventoryStatus,
                                              Pageable pageable) {
        Specification<Inventory> spec = buildSpec(search, warehouseId, binId, productId, skuId, inventoryStatus);
        return PageResponse.of(repository.findAll(spec, pageable).map(e -> enrich(mapper.toDto(e), e)));
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteById(Long id) {
        Inventory entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted inventory id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Inventory> entities = repository.findAllById(ids).stream()
                .filter(i -> !i.isDeleted())
                .toList();
        entities.forEach(i -> i.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} inventory records", entities.size());
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Inventory findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inventory not found with id: " + id));
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

    private Long resolveProductId(String productName) {
        return productRepository.findByProductNameAndDeletedFalse(productName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product not found with name: " + productName))
                .getId();
    }

    private Long resolveSkuId(String skuCode, Long productId) {
        return skuRepository.findBySkuCodeAndProductIdAndDeletedFalse(skuCode, productId)
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

    private void validateLotSerial(InventoryDto dto, Long productId) {
        productRepository.findById(productId).ifPresent(product -> {
            if (product.isLotTracking() && (dto.getLotNumber() == null || dto.getLotNumber().isBlank())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Lot number is required for lot-tracked products");
            }
            if (product.isSerialTracking() && (dto.getSerialNumber() == null || dto.getSerialNumber().isBlank())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Serial number is required for serial-tracked products");
            }
        });
    }

    private void validateQuantities(InventoryDto dto) {
        if (dto.getReservedQuantity().compareTo(dto.getOnHandQuantity()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Reserved quantity cannot exceed on-hand quantity");
        }
        if (dto.getAllocatedQuantity().compareTo(dto.getOnHandQuantity()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Allocated quantity cannot exceed on-hand quantity");
        }
    }

    private void checkDuplicateOnCreate(Long warehouseId, Long binId, Long skuId,
                                         String lot, String serial, InventoryStatus status) {
        if (repository.existsDuplicateOnCreate(warehouseId, binId, skuId, lot, serial, status)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Inventory record already exists for the given warehouse, bin, SKU, lot, serial and status combination");
        }
    }

    private void checkDuplicateOnUpdate(Long warehouseId, Long binId, Long skuId,
                                         String lot, String serial, InventoryStatus status, Long excludeId) {
        if (repository.existsDuplicateOnUpdate(warehouseId, binId, skuId, lot, serial, status, excludeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Inventory record already exists for the given warehouse, bin, SKU, lot, serial and status combination");
        }
    }

    /** Populate String FK fields on the DTO from the entity's Long IDs. */
    private InventoryDto enrich(InventoryDto dto, Inventory entity) {
        warehouseRepository.findById(entity.getWarehouseId())
                .ifPresent(w -> dto.setWarehouseId(w.getWarehouseName()));
        binRepository.findById(entity.getBinId())
                .ifPresent(b -> dto.setBinId(b.getBinCode()));
        productRepository.findById(entity.getProductId())
                .ifPresent(p -> dto.setProductId(p.getProductName()));
        skuRepository.findById(entity.getSkuId())
                .ifPresent(s -> dto.setSkuId(s.getSkuCode()));
        uomRepository.findById(entity.getUomId())
                .ifPresent(u -> dto.setUomId(u.getUomName()));
        return dto;
    }

    private Specification<Inventory> buildSpec(String search,
                                               Long warehouseId,
                                               Long binId,
                                               Long productId,
                                               Long skuId,
                                               InventoryStatus inventoryStatus) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (warehouseId != null)      pred = cb.and(pred, cb.equal(root.get("warehouseId"), warehouseId));
            if (binId != null)            pred = cb.and(pred, cb.equal(root.get("binId"), binId));
            if (productId != null)        pred = cb.and(pred, cb.equal(root.get("productId"), productId));
            if (skuId != null)            pred = cb.and(pred, cb.equal(root.get("skuId"), skuId));
            if (inventoryStatus != null)  pred = cb.and(pred, cb.equal(root.get("inventoryStatus"), inventoryStatus));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.or(
                        cb.like(cb.lower(root.get("lotNumber")),    pattern),
                        cb.like(cb.lower(root.get("serialNumber")), pattern)
                ));
            }
            return pred;
        };
    }
}
