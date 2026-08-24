package com.supplychain.procurement.purchaseorder.application.service;

import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.procurement.purchaseorder.application.dto.PurchaseOrderDto;
import com.supplychain.procurement.purchaseorder.application.dto.PurchaseOrderLineItemDto;
import com.supplychain.procurement.purchaseorder.domain.enums.PoStatus;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrder;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrderLineItem;
import com.supplychain.procurement.purchaseorder.infrastructure.mapper.PurchaseOrderLineItemMapper;
import com.supplychain.procurement.purchaseorder.infrastructure.mapper.PurchaseOrderMapper;
import com.supplychain.procurement.purchaseorder.infrastructure.repository.PurchaseOrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository     repository;
    private final PurchaseOrderMapper         mapper;
    private final PurchaseOrderLineItemMapper lineItemMapper;

    @Override
    @Transactional
    public PurchaseOrderDto save(PurchaseOrderDto dto) {
        log.info("Creating Purchase Order with PO Number: {}", dto.getPoNumber());

        if (repository.existsByPoNumberAndDeletedFalse(dto.getPoNumber())) {
            log.warn("Duplicate PO number: {}", dto.getPoNumber());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Purchase Order number already exists: " + dto.getPoNumber());
        }

        validateDates(dto);
        validateLineItems(dto.getLineItems());

        PurchaseOrder entity = mapper.toEntity(dto);
        entity.setDeleted(false);

        // Process line items and compute totals
        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        for (PurchaseOrderLineItemDto lineDto : dto.getLineItems()) {
            PurchaseOrderLineItem lineItem = lineItemMapper.toEntity(lineDto);
            lineItem.setDeleted(false);

            BigDecimal lineTotal = computeLineTotal(lineDto);
            lineItem.setTotalMonetaryValue(lineTotal);
            calculatedSubtotal = calculatedSubtotal.add(lineTotal);

            entity.addLineItem(lineItem);
        }

        entity.setSubtotalAmount(calculatedSubtotal);
        entity.setTotalAmount(computeTotalAmount(calculatedSubtotal, dto.getDiscountAmount(), dto.getFreightAmount(), dto.getTaxAmount()));

        PurchaseOrder saved = repository.save(entity);
        log.info("Created Purchase Order id={}, poNumber={} with {} line items", saved.getId(), saved.getPoNumber(), saved.getLineItems().size());

        return enrichDto(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderDto update(Long id, PurchaseOrderDto dto) {
        log.info("Updating Purchase Order id: {}", id);

        PurchaseOrder entity = findActiveOrThrow(id);

        if (repository.existsByPoNumberAndIdNotAndDeletedFalse(dto.getPoNumber(), id)) {
            log.warn("Duplicate PO number {} on update for id {}", dto.getPoNumber(), id);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Purchase Order number already exists: " + dto.getPoNumber());
        }

        validateDates(dto);
        validateLineItems(dto.getLineItems());

        mapper.updateEntity(dto, entity);

        // Synchronize child line items
        Map<Long, PurchaseOrderLineItem> existingLineMap = entity.getLineItems().stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(PurchaseOrderLineItem::getId, Function.identity()));

        List<PurchaseOrderLineItem> updatedLines = new ArrayList<>();
        Set<Long> processedIds = new HashSet<>();
        BigDecimal calculatedSubtotal = BigDecimal.ZERO;

        for (PurchaseOrderLineItemDto lineDto : dto.getLineItems()) {
            BigDecimal lineTotal = computeLineTotal(lineDto);

            if (lineDto.getId() != null && existingLineMap.containsKey(lineDto.getId())) {
                PurchaseOrderLineItem existingItem = existingLineMap.get(lineDto.getId());
                lineItemMapper.updateEntity(lineDto, existingItem);
                existingItem.setTotalMonetaryValue(lineTotal);
                existingItem.setDeleted(false);
                processedIds.add(lineDto.getId());
                updatedLines.add(existingItem);
            } else {
                PurchaseOrderLineItem newItem = lineItemMapper.toEntity(lineDto);
                newItem.setTotalMonetaryValue(lineTotal);
                newItem.setDeleted(false);
                newItem.setPurchaseOrder(entity);
                updatedLines.add(newItem);
            }

            calculatedSubtotal = calculatedSubtotal.add(lineTotal);
        }

        // Remove orphans
        List<PurchaseOrderLineItem> toRemove = entity.getLineItems().stream()
                .filter(item -> item.getId() != null && !processedIds.contains(item.getId()))
                .toList();
        for (PurchaseOrderLineItem item : toRemove) {
            entity.removeLineItem(item);
        }

        // Clear and reset line items list to maintain managed collection
        entity.getLineItems().clear();
        for (PurchaseOrderLineItem item : updatedLines) {
            entity.addLineItem(item);
        }

        entity.setSubtotalAmount(calculatedSubtotal);
        entity.setTotalAmount(computeTotalAmount(calculatedSubtotal, dto.getDiscountAmount(), dto.getFreightAmount(), dto.getTaxAmount()));

        PurchaseOrder saved = repository.save(entity);
        log.info("Updated Purchase Order id={}", saved.getId());

        return enrichDto(saved);
    }

    @Override
    public PurchaseOrderDto findById(Long id) {
        PurchaseOrder entity = findActiveOrThrow(id);
        return enrichDto(entity);
    }

    @Override
    public PageResponse<PurchaseOrderDto> findAll(String search,
                                                  PoStatus poStatus,
                                                  String supplier,
                                                  String warehouse,
                                                  LocalDate startDate,
                                                  LocalDate endDate,
                                                  Pageable pageable) {
        Specification<PurchaseOrder> spec = buildSpec(search, poStatus, supplier, warehouse, startDate, endDate);
        return PageResponse.of(repository.findAll(spec, pageable).map(this::enrichDto));
    }

    @Override
    public List<DropdownOptionDto> findDropdown() {
        return repository.findAllByDeletedFalseOrderByPoNumberAsc()
                .stream()
                .map(po -> new DropdownOptionDto(po.getPoNumber(), po.getPoNumber()))
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        PurchaseOrder entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        if (entity.getLineItems() != null) {
            entity.getLineItems().forEach(item -> item.setDeleted(true));
        }
        repository.save(entity);
        log.info("Soft-deleted Purchase Order id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<PurchaseOrder> entities = repository.findAllById(ids).stream()
                .filter(po -> !po.isDeleted())
                .toList();

        entities.forEach(po -> {
            po.setDeleted(true);
            if (po.getLineItems() != null) {
                po.getLineItems().forEach(item -> item.setDeleted(true));
            }
        });

        repository.saveAll(entities);
        log.info("Soft-deleted {} Purchase Orders", entities.size());
    }

    // -------------------------------------------------------------------------
    // Helper & Validation Methods
    // -------------------------------------------------------------------------

    private PurchaseOrder findActiveOrThrow(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Purchase Order not found with id: " + id));
    }

    private void validateDates(PurchaseOrderDto dto) {
        LocalDate requested = dto.getRequestedDeliveryDate();
        LocalDate promised  = dto.getPromisedDeliveryDate();
        LocalDate latest    = dto.getLatestDeliveryDate();

        if (requested != null && promised != null && requested.isAfter(promised)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Requested delivery date cannot be after promised delivery date");
        }
        if (promised != null && latest != null && promised.isAfter(latest)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Promised delivery date cannot be after latest delivery date");
        }
        if (requested != null && latest != null && requested.isAfter(latest)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Requested delivery date cannot be after latest delivery date");
        }
    }

    private void validateLineItems(List<PurchaseOrderLineItemDto> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Purchase order must contain at least one line item");
        }

        Set<Integer> lineNumbers = new HashSet<>();
        for (PurchaseOrderLineItemDto item : lineItems) {
            if (item.getLineNumber() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Line number is required");
            }
            if (!lineNumbers.add(item.getLineNumber())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Duplicate line number within purchase order: " + item.getLineNumber());
            }
            if (item.getQuantityOrdered() == null || item.getQuantityOrdered().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Quantity ordered must be greater than 0 for line " + item.getLineNumber());
            }
            if (item.getOriginalOrderQuantity() == null || item.getOriginalOrderQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Original order quantity must be greater than 0 for line " + item.getLineNumber());
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unit price cannot be negative for line " + item.getLineNumber());
            }
            if (item.getQuantityReceived() != null) {
                if (item.getQuantityReceived().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Quantity received cannot be negative for line " + item.getLineNumber());
                }
                if (item.getQuantityReceived().compareTo(item.getQuantityOrdered()) > 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Quantity received cannot exceed quantity ordered for line " + item.getLineNumber());
                }
            }
            if (item.getShippedQuantity() != null) {
                if (item.getShippedQuantity().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Shipped quantity cannot be negative for line " + item.getLineNumber());
                }
                if (item.getShippedQuantity().compareTo(item.getQuantityOrdered()) > 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Shipped quantity cannot exceed quantity ordered for line " + item.getLineNumber());
                }
            }
        }
    }

    private BigDecimal computeLineTotal(PurchaseOrderLineItemDto lineDto) {
        if (lineDto.getQuantityOrdered() != null && lineDto.getUnitPrice() != null) {
            return lineDto.getQuantityOrdered().multiply(lineDto.getUnitPrice());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal computeTotalAmount(BigDecimal subtotal, BigDecimal discount, BigDecimal freight, BigDecimal tax) {
        BigDecimal disc = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal frt  = freight != null ? freight : BigDecimal.ZERO;
        BigDecimal tx   = tax != null ? tax : BigDecimal.ZERO;
        return subtotal.subtract(disc).add(frt).add(tx);
    }

    private PurchaseOrderDto enrichDto(PurchaseOrder entity) {
        PurchaseOrderDto dto = mapper.toDto(entity);
        if (entity.getLineItems() != null) {
            List<PurchaseOrderLineItemDto> activeLineDtos = entity.getLineItems().stream()
                    .filter(item -> !item.isDeleted())
                    .map(lineItemMapper::toDto)
                    .toList();
            dto.setLineItems(activeLineDtos);
        }
        return dto;
    }

    private Specification<PurchaseOrder> buildSpec(String search,
                                                   PoStatus poStatus,
                                                   String supplier,
                                                   String warehouse,
                                                   LocalDate startDate,
                                                   LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));

            if (poStatus != null) {
                predicates.add(cb.equal(root.get("poStatus"), poStatus));
            }
            if (supplier != null && !supplier.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("supplier")), "%" + supplier.toLowerCase() + "%"));
            }
            if (warehouse != null && !warehouse.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("warehouse")), "%" + warehouse.toLowerCase() + "%"));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("poDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("poDate"), endDate));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("poNumber")), pattern),
                        cb.like(cb.lower(root.get("supplier")), pattern),
                        cb.like(cb.lower(root.get("notes")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
