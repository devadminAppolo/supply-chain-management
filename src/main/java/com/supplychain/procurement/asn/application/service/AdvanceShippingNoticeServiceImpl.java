package com.supplychain.procurement.asn.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.procurement.asn.application.dto.AsnCreateRequestDto;
import com.supplychain.procurement.asn.application.dto.AsnLineRequestDto;
import com.supplychain.procurement.asn.application.dto.AsnLineResponseDto;
import com.supplychain.procurement.asn.application.dto.AsnLineUpdateRequestDto;
import com.supplychain.procurement.asn.application.dto.AsnResponseDto;
import com.supplychain.procurement.asn.application.dto.AsnUpdateRequestDto;
import com.supplychain.procurement.asn.application.dto.AvailablePurchaseOrderDto;
import com.supplychain.procurement.asn.application.dto.AvailablePurchaseOrderLineDto;
import com.supplychain.procurement.asn.domain.enums.AsnLineStatus;
import com.supplychain.procurement.asn.domain.enums.AsnStatus;
import com.supplychain.procurement.asn.domain.model.AdvanceShippingNotice;
import com.supplychain.procurement.asn.domain.model.AdvanceShippingNoticeLine;
import com.supplychain.procurement.asn.infrastructure.mapper.AdvanceShippingNoticeLineMapper;
import com.supplychain.procurement.asn.infrastructure.mapper.AdvanceShippingNoticeMapper;
import com.supplychain.procurement.asn.infrastructure.repository.AdvanceShippingNoticeLineRepository;
import com.supplychain.procurement.asn.infrastructure.repository.AdvanceShippingNoticeRepository;
import com.supplychain.procurement.purchaseorder.domain.enums.LineStatus;
import com.supplychain.procurement.purchaseorder.domain.enums.PoStatus;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrder;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrderLineItem;
import com.supplychain.procurement.purchaseorder.infrastructure.repository.PurchaseOrderLineItemRepository;
import com.supplychain.procurement.purchaseorder.infrastructure.repository.PurchaseOrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
public class AdvanceShippingNoticeServiceImpl implements AdvanceShippingNoticeService {

    private final AdvanceShippingNoticeRepository     asnRepository;
    private final AdvanceShippingNoticeLineRepository asnLineRepository;
    private final PurchaseOrderRepository             poRepository;
    private final PurchaseOrderLineItemRepository     poLineRepository;
    private final AdvanceShippingNoticeMapper         asnMapper;
    private final AdvanceShippingNoticeLineMapper     asnLineMapper;

    @Override
    @Transactional
    public AsnResponseDto createAsn(AsnCreateRequestDto request) {
        log.info("Creating Advance Shipping Notice for Purchase Order ID: {}", request.getPurchaseOrderId());

        PurchaseOrder po = validateAndGetEligiblePo(request.getPurchaseOrderId());

        // Validate ASN Number
        String asnNumber = request.getAsnNumber();
        if (asnNumber != null && !asnNumber.isBlank()) {
            if (asnRepository.existsByAsnNumberAndDeletedFalse(asnNumber.trim())) {
                log.warn("Duplicate ASN number: {}", asnNumber);
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Advance Shipping Notice number already exists: " + asnNumber);
            }
            asnNumber = asnNumber.trim();
        } else {
            asnNumber = generateAsnNumber();
        }

        List<AsnLineRequestDto> requestLines = request.getLines();
        if (requestLines == null || requestLines.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Advance Shipping Notice must contain at least one line item");
        }

        // Validate duplicate PO line references in request
        Set<Long> poLineIds = new HashSet<>();
        for (AsnLineRequestDto lineDto : requestLines) {
            if (lineDto.getPoLineId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PO line ID is required for each ASN line");
            }
            if (!poLineIds.add(lineDto.getPoLineId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Duplicate PO line ID in request: " + lineDto.getPoLineId());
            }
        }

        // Concurrency Control: Lock all requested PO line items
        List<PurchaseOrderLineItem> lockedPoLines = poLineRepository.findAllByIdInForUpdate(new ArrayList<>(poLineIds));
        Map<Long, PurchaseOrderLineItem> poLineMap = lockedPoLines.stream()
                .collect(Collectors.toMap(PurchaseOrderLineItem::getId, Function.identity()));

        if (poLineMap.size() != poLineIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "One or more referenced PO lines do not exist");
        }

        AdvanceShippingNotice asn = asnMapper.toEntity(request);
        asn.setAsnNumber(asnNumber);
        asn.setPurchaseOrder(po);
        asn.setAsnStatus(AsnStatus.SUBMITTED);
        asn.setStatus(AsnStatus.SUBMITTED.name());
        asn.setDeleted(false);

        // Derive header supplier & warehouse if absent
        if (asn.getSupplierId() == null || asn.getSupplierId().isBlank()) {
            asn.setSupplierId(po.getSupplier());
        }
        if (asn.getSupplierName() == null || asn.getSupplierName().isBlank()) {
            asn.setSupplierName(po.getSupplier());
        }
        if (asn.getWarehouseId() == null || asn.getWarehouseId().isBlank()) {
            asn.setWarehouseId(po.getWarehouse() != null ? po.getWarehouse() : po.getDistributionCenter());
        }
        if (asn.getWarehouseName() == null || asn.getWarehouseName().isBlank()) {
            asn.setWarehouseName(po.getWarehouse() != null ? po.getWarehouse() : po.getDistributionCenter());
        }

        int lineIndex = 1;
        BigDecimal calculatedTotalWeight = BigDecimal.ZERO;
        BigDecimal calculatedTotalVolume = BigDecimal.ZERO;

        for (AsnLineRequestDto lineDto : requestLines) {
            PurchaseOrderLineItem poLine = poLineMap.get(lineDto.getPoLineId());

            validatePoLineOwnershipAndEligibility(poLine, po.getId());

            BigDecimal orderedQuantity = poLine.getQuantityOrdered();
            BigDecimal previouslyShipped = asnLineRepository.sumShippedQuantityByPoLineId(poLine.getId());
            BigDecimal remainingQuantity = orderedQuantity.subtract(previouslyShipped);
            BigDecimal requestedQuantity = lineDto.getShipmentQuantity();

            if (requestedQuantity == null || requestedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Shipment quantity must be greater than 0 for PO line " + poLine.getLineNumber());
            }

            // Over-shipment check
            if (requestedQuantity.compareTo(remainingQuantity) > 0) {
                log.warn("Over-shipment rejected: PO {}, Line {}, Ordered {}, PrevShipped {}, Remaining {}, Requested {}",
                        po.getPoNumber(), poLine.getLineNumber(), orderedQuantity, previouslyShipped, remainingQuantity, requestedQuantity);
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        String.format("Over-shipment rejected for PO [%s], Line [%d] (Item: %s). Ordered: %s, Previously Shipped: %s, Remaining: %s, Requested: %s",
                                po.getPoNumber(), poLine.getLineNumber(), poLine.getItemCode(), orderedQuantity, previouslyShipped, remainingQuantity, requestedQuantity));
            }

            BigDecimal lineUnitPrice = poLine.getUnitPrice();
            BigDecimal lineTotalValue = requestedQuantity.multiply(lineUnitPrice);
            BigDecimal newShippedTotal = previouslyShipped.add(requestedQuantity);
            BigDecimal newRemaining = orderedQuantity.subtract(newShippedTotal);

            AdvanceShippingNoticeLine asnLine = asnLineMapper.toEntity(lineDto);
            asnLine.setAdvanceShippingNotice(asn);
            asnLine.setPurchaseOrderLine(poLine);
            asnLine.setLineNumber(lineIndex++);
            asnLine.setItemCode(poLine.getItemCode());
            asnLine.setItemName(poLine.getItemName());
            asnLine.setSupplierItemCode(poLine.getSupplierItemCode());
            asnLine.setItemGtin(poLine.getItemGtin());
            asnLine.setQuantityOrdered(orderedQuantity);
            asnLine.setPreviouslyShippedQuantity(previouslyShipped);
            asnLine.setShipmentQuantity(requestedQuantity);
            asnLine.setRemainingQuantity(newRemaining);
            asnLine.setQuantityUom(poLine.getQuantityUom());
            asnLine.setUnitPrice(lineUnitPrice);
            asnLine.setCurrencyCode(poLine.getCurrencyCode());
            asnLine.setTotalMonetaryValue(lineTotalValue);
            asnLine.setLineStatus(newShippedTotal.compareTo(orderedQuantity) >= 0 ? AsnLineStatus.SHIPPED : AsnLineStatus.PARTIALLY_SHIPPED);
            asnLine.setDeleted(false);

            if (lineDto.getWeight() != null) {
                calculatedTotalWeight = calculatedTotalWeight.add(lineDto.getWeight());
            }
            if (lineDto.getVolume() != null) {
                calculatedTotalVolume = calculatedTotalVolume.add(lineDto.getVolume());
            }

            // Update PO Line entity
            poLine.setShippedQuantity(newShippedTotal);
            poLine.setLineStatus(newShippedTotal.compareTo(orderedQuantity) >= 0 ? LineStatus.SHIPPED : LineStatus.PARTIALLY_SHIPPED);

            asn.addLine(asnLine);
        }

        if (asn.getTotalWeight() == null && calculatedTotalWeight.compareTo(BigDecimal.ZERO) > 0) {
            asn.setTotalWeight(calculatedTotalWeight);
        }
        if (asn.getTotalVolume() == null && calculatedTotalVolume.compareTo(BigDecimal.ZERO) > 0) {
            asn.setTotalVolume(calculatedTotalVolume);
        }

        // Save PO Lines
        poLineRepository.saveAll(lockedPoLines);

        // Update overall PO shipment status
        updatePoShipmentStatus(po);
        poRepository.save(po);

        AdvanceShippingNotice savedAsn = asnRepository.save(asn);
        log.info("Successfully created ASN id={}, asnNumber={} with {} lines for PO {}",
                savedAsn.getId(), savedAsn.getAsnNumber(), savedAsn.getLines().size(), po.getPoNumber());

        return enrichAsnDto(savedAsn);
    }

    @Override
    @Transactional
    public AsnResponseDto updateAsn(Long id, AsnUpdateRequestDto request) {
        log.info("Updating Advance Shipping Notice id: {}", id);

        AdvanceShippingNotice entity = findActiveOrThrow(id);

        if (entity.getAsnStatus() == AsnStatus.CANCELLED || entity.getAsnStatus() == AsnStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot update an Advance Shipping Notice in " + entity.getAsnStatus() + " status");
        }

        if (request.getAsnNumber() != null && !request.getAsnNumber().isBlank()) {
            if (asnRepository.existsByAsnNumberAndIdNotAndDeletedFalse(request.getAsnNumber().trim(), id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Advance Shipping Notice number already exists: " + request.getAsnNumber());
            }
        }

        PurchaseOrder po = entity.getPurchaseOrder();

        List<AsnLineUpdateRequestDto> requestLines = request.getLines();
        if (requestLines == null || requestLines.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Advance Shipping Notice must contain at least one line item");
        }

        Set<Long> requestedPoLineIds = new HashSet<>();
        for (AsnLineUpdateRequestDto lineDto : requestLines) {
            if (lineDto.getPoLineId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PO line ID is required for each ASN line");
            }
            if (!requestedPoLineIds.add(lineDto.getPoLineId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Duplicate PO line ID in request: " + lineDto.getPoLineId());
            }
        }

        // Gather all PO line IDs involved (both existing ASN lines and newly requested)
        Set<Long> allInvolvedPoLineIds = new HashSet<>(requestedPoLineIds);
        entity.getLines().forEach(l -> allInvolvedPoLineIds.add(l.getPurchaseOrderLine().getId()));

        // Pessimistic lock all involved PO lines
        List<PurchaseOrderLineItem> lockedPoLines = poLineRepository.findAllByIdInForUpdate(new ArrayList<>(allInvolvedPoLineIds));
        Map<Long, PurchaseOrderLineItem> poLineMap = lockedPoLines.stream()
                .collect(Collectors.toMap(PurchaseOrderLineItem::getId, Function.identity()));

        asnMapper.updateEntity(request, entity);

        Map<Long, AdvanceShippingNoticeLine> existingLineMap = entity.getLines().stream()
                .filter(l -> l.getId() != null)
                .collect(Collectors.toMap(AdvanceShippingNoticeLine::getId, Function.identity()));

        List<AdvanceShippingNoticeLine> updatedLines = new ArrayList<>();
        Set<Long> processedLineIds = new HashSet<>();
        int lineIndex = 1;

        for (AsnLineUpdateRequestDto lineDto : requestLines) {
            PurchaseOrderLineItem poLine = poLineMap.get(lineDto.getPoLineId());
            validatePoLineOwnershipAndEligibility(poLine, po.getId());

            BigDecimal orderedQuantity = poLine.getQuantityOrdered();
            // Calculate previously shipped excluding this ASN
            BigDecimal previouslyShipped = asnLineRepository.sumShippedQuantityByPoLineIdExcludingAsn(poLine.getId(), id);
            BigDecimal remainingQuantity = orderedQuantity.subtract(previouslyShipped);
            BigDecimal requestedQuantity = lineDto.getShipmentQuantity();

            if (requestedQuantity == null || requestedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Shipment quantity must be greater than 0 for PO line " + poLine.getLineNumber());
            }

            if (requestedQuantity.compareTo(remainingQuantity) > 0) {
                log.warn("Over-shipment on update rejected: PO {}, Line {}, Ordered {}, PrevShipped {}, Remaining {}, Requested {}",
                        po.getPoNumber(), poLine.getLineNumber(), orderedQuantity, previouslyShipped, remainingQuantity, requestedQuantity);
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        String.format("Over-shipment rejected on update for PO [%s], Line [%d] (Item: %s). Ordered: %s, Previously Shipped: %s, Remaining: %s, Requested: %s",
                                po.getPoNumber(), poLine.getLineNumber(), poLine.getItemCode(), orderedQuantity, previouslyShipped, remainingQuantity, requestedQuantity));
            }

            BigDecimal lineUnitPrice = poLine.getUnitPrice();
            BigDecimal lineTotalValue = requestedQuantity.multiply(lineUnitPrice);
            BigDecimal newShippedTotal = previouslyShipped.add(requestedQuantity);
            BigDecimal newRemaining = orderedQuantity.subtract(newShippedTotal);

            AdvanceShippingNoticeLine asnLine;
            if (lineDto.getId() != null && existingLineMap.containsKey(lineDto.getId())) {
                asnLine = existingLineMap.get(lineDto.getId());
                asnLineMapper.updateEntity(lineDto, asnLine);
                processedLineIds.add(lineDto.getId());
            } else {
                asnLine = asnLineMapper.toEntity(toCreateLineDto(lineDto));
            }

            asnLine.setAdvanceShippingNotice(entity);
            asnLine.setPurchaseOrderLine(poLine);
            asnLine.setLineNumber(lineIndex++);
            asnLine.setItemCode(poLine.getItemCode());
            asnLine.setItemName(poLine.getItemName());
            asnLine.setSupplierItemCode(poLine.getSupplierItemCode());
            asnLine.setItemGtin(poLine.getItemGtin());
            asnLine.setQuantityOrdered(orderedQuantity);
            asnLine.setPreviouslyShippedQuantity(previouslyShipped);
            asnLine.setShipmentQuantity(requestedQuantity);
            asnLine.setRemainingQuantity(newRemaining);
            asnLine.setQuantityUom(poLine.getQuantityUom());
            asnLine.setUnitPrice(lineUnitPrice);
            asnLine.setCurrencyCode(poLine.getCurrencyCode());
            asnLine.setTotalMonetaryValue(lineTotalValue);
            asnLine.setLineStatus(newShippedTotal.compareTo(orderedQuantity) >= 0 ? AsnLineStatus.SHIPPED : AsnLineStatus.PARTIALLY_SHIPPED);
            asnLine.setDeleted(false);

            poLine.setShippedQuantity(newShippedTotal);
            poLine.setLineStatus(newShippedTotal.compareTo(orderedQuantity) >= 0 ? LineStatus.SHIPPED : (newShippedTotal.compareTo(BigDecimal.ZERO) > 0 ? LineStatus.PARTIALLY_SHIPPED : LineStatus.OPEN));

            updatedLines.add(asnLine);
        }

        // Reconcile removed lines
        for (AdvanceShippingNoticeLine existingLine : entity.getLines()) {
            if (existingLine.getId() != null && !processedLineIds.contains(existingLine.getId())) {
                PurchaseOrderLineItem poLine = existingLine.getPurchaseOrderLine();
                BigDecimal recalcShipped = asnLineRepository.sumShippedQuantityByPoLineIdExcludingAsn(poLine.getId(), id);
                poLine.setShippedQuantity(recalcShipped);
                poLine.setLineStatus(recalcShipped.compareTo(poLine.getQuantityOrdered()) >= 0 ? LineStatus.SHIPPED : (recalcShipped.compareTo(BigDecimal.ZERO) > 0 ? LineStatus.PARTIALLY_SHIPPED : LineStatus.OPEN));
            }
        }

        entity.getLines().clear();
        for (AdvanceShippingNoticeLine line : updatedLines) {
            entity.addLine(line);
        }

        poLineRepository.saveAll(lockedPoLines);
        updatePoShipmentStatus(po);
        poRepository.save(po);

        AdvanceShippingNotice saved = asnRepository.save(entity);
        log.info("Updated ASN id={}", saved.getId());

        return enrichAsnDto(saved);
    }

    @Override
    public AsnResponseDto findById(Long id) {
        AdvanceShippingNotice entity = findActiveOrThrow(id);
        return enrichAsnDto(entity);
    }

    @Override
    public PageResponse<AsnResponseDto> findAll(String search,
                                                AsnStatus status,
                                                Long purchaseOrderId,
                                                String supplierId,
                                                String warehouseId,
                                                LocalDate startDate,
                                                LocalDate endDate,
                                                Pageable pageable) {
        Specification<AdvanceShippingNotice> spec = buildSpec(search, status, purchaseOrderId, supplierId, warehouseId, startDate, endDate);
        return PageResponse.of(asnRepository.findAll(spec, pageable).map(this::enrichAsnDto));
    }

    @Override
    @Transactional
    public AsnResponseDto cancelAsn(Long id) {
        log.info("Cancelling Advance Shipping Notice id: {}", id);

        AdvanceShippingNotice entity = findActiveOrThrow(id);
        if (entity.getAsnStatus() == AsnStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Advance Shipping Notice is already cancelled");
        }

        entity.setAsnStatus(AsnStatus.CANCELLED);
        entity.setStatus(AsnStatus.CANCELLED.name());
        entity.getLines().forEach(line -> line.setLineStatus(AsnLineStatus.CANCELLED));

        PurchaseOrder po = entity.getPurchaseOrder();

        // Lock PO lines to safely recalculate
        List<Long> poLineIds = entity.getLines().stream()
                .map(l -> l.getPurchaseOrderLine().getId())
                .distinct()
                .toList();

        List<PurchaseOrderLineItem> lockedPoLines = poLineRepository.findAllByIdInForUpdate(poLineIds);
        for (PurchaseOrderLineItem poLine : lockedPoLines) {
            BigDecimal recalcShipped = asnLineRepository.sumShippedQuantityByPoLineIdExcludingAsn(poLine.getId(), id);
            poLine.setShippedQuantity(recalcShipped);
            if (recalcShipped.compareTo(BigDecimal.ZERO) == 0) {
                poLine.setLineStatus(LineStatus.OPEN);
            } else if (recalcShipped.compareTo(poLine.getQuantityOrdered()) >= 0) {
                poLine.setLineStatus(LineStatus.SHIPPED);
            } else {
                poLine.setLineStatus(LineStatus.PARTIALLY_SHIPPED);
            }
        }
        poLineRepository.saveAll(lockedPoLines);

        updatePoShipmentStatus(po);
        poRepository.save(po);

        AdvanceShippingNotice saved = asnRepository.save(entity);
        log.info("Cancelled ASN id={} and reverted PO shipment quantities", id);

        return enrichAsnDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        AdvanceShippingNotice entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        if (entity.getLines() != null) {
            entity.getLines().forEach(l -> l.setDeleted(true));
        }
        asnRepository.save(entity);
        log.info("Soft-deleted ASN id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<AdvanceShippingNotice> entities = asnRepository.findAllById(ids).stream()
                .filter(asn -> !asn.isDeleted())
                .toList();

        entities.forEach(asn -> {
            asn.setDeleted(true);
            if (asn.getLines() != null) {
                asn.getLines().forEach(l -> l.setDeleted(true));
            }
        });

        asnRepository.saveAll(entities);
        log.info("Soft-deleted {} ASNs", entities.size());
    }

    @Override
    public PageResponse<AvailablePurchaseOrderDto> getAvailablePurchaseOrders(String search, Pageable pageable) {
        Specification<PurchaseOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(root.get("poStatus").in(
                    PoStatus.APPROVED,
                    PoStatus.SENT_TO_SUPPLIER,
                    PoStatus.SUPPLIER_CONFIRMED,
                    PoStatus.PARTIALLY_CONFIRMED,
                    PoStatus.PARTIALLY_SHIPPED
            ));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("poNumber")), pattern),
                        cb.like(cb.lower(root.get("supplier")), pattern),
                        cb.like(cb.lower(root.get("warehouse")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<PurchaseOrder> poPage = poRepository.findAll(spec, pageable);

        List<AvailablePurchaseOrderDto> availableDtos = poPage.getContent().stream()
                .map(po -> {
                    List<PurchaseOrderLineItem> activeLines = poLineRepository.findByPurchaseOrderIdAndDeletedFalseOrderByLineNumberAsc(po.getId());
                    int shippableCount = 0;
                    for (PurchaseOrderLineItem line : activeLines) {
                        if (line.getLineStatus() != LineStatus.CANCELLED && line.getLineStatus() != LineStatus.CLOSED && line.getLineStatus() != LineStatus.REJECTED) {
                            BigDecimal shipped = asnLineRepository.sumShippedQuantityByPoLineId(line.getId());
                            if (line.getQuantityOrdered().subtract(shipped).compareTo(BigDecimal.ZERO) > 0) {
                                shippableCount++;
                            }
                        }
                    }

                    if (shippableCount > 0) {
                        return AvailablePurchaseOrderDto.builder()
                                .id(po.getId())
                                .poNumber(po.getPoNumber())
                                .poDate(po.getPoDate())
                                .poStatus(po.getPoStatus())
                                .supplier(po.getSupplier())
                                .warehouse(po.getWarehouse())
                                .distributionCenter(po.getDistributionCenter())
                                .currencyCode(po.getCurrencyCode())
                                .totalLines(activeLines.size())
                                .shippableLinesCount(shippableCount)
                                .build();
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .toList();

        Page<AvailablePurchaseOrderDto> resultPage = new PageImpl<>(availableDtos, pageable, poPage.getTotalElements());
        return PageResponse.of(resultPage);
    }

    @Override
    public List<AvailablePurchaseOrderLineDto> getAvailablePoLines(Long poId) {
        PurchaseOrder po = validateAndGetEligiblePo(poId);
        List<PurchaseOrderLineItem> activeLines = poLineRepository.findByPurchaseOrderIdAndDeletedFalseOrderByLineNumberAsc(po.getId());

        List<AvailablePurchaseOrderLineDto> availableLines = new ArrayList<>();
        for (PurchaseOrderLineItem line : activeLines) {
            if (line.getLineStatus() == LineStatus.CANCELLED || line.getLineStatus() == LineStatus.CLOSED || line.getLineStatus() == LineStatus.REJECTED) {
                continue;
            }

            BigDecimal previouslyShipped = asnLineRepository.sumShippedQuantityByPoLineId(line.getId());
            BigDecimal remaining = line.getQuantityOrdered().subtract(previouslyShipped);

            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                availableLines.add(AvailablePurchaseOrderLineDto.builder()
                        .poLineId(line.getId())
                        .lineNumber(line.getLineNumber())
                        .itemCode(line.getItemCode())
                        .itemName(line.getItemName())
                        .supplierItemCode(line.getSupplierItemCode())
                        .itemGtin(line.getItemGtin())
                        .orderedQuantity(line.getQuantityOrdered())
                        .previouslyShippedQuantity(previouslyShipped)
                        .remainingQuantity(remaining)
                        .quantityUom(line.getQuantityUom())
                        .unitPrice(line.getUnitPrice())
                        .currencyCode(line.getCurrencyCode())
                        .lineStatus(line.getLineStatus())
                        .build());
            }
        }

        return availableLines;
    }

    // -------------------------------------------------------------------------
    // Helper & Validation Methods
    // -------------------------------------------------------------------------

    private PurchaseOrder validateAndGetEligiblePo(Long poId) {
        PurchaseOrder po = poRepository.findByIdAndDeletedFalse(poId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Purchase Order not found with id: " + poId));

        if (po.getPoStatus() == PoStatus.CANCELLED || po.getPoStatus() == PoStatus.CLOSED || po.getPoStatus() == PoStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Purchase Order is not eligible for shipping in status: " + po.getPoStatus());
        }

        if (po.getPoStatus() == PoStatus.DRAFT || po.getPoStatus() == PoStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Purchase Order must be approved before shipping. Current status: " + po.getPoStatus());
        }

        return po;
    }

    private void validatePoLineOwnershipAndEligibility(PurchaseOrderLineItem poLine, Long poId) {
        if (poLine.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Referenced PO line is deleted: " + poLine.getId());
        }
        if (!poLine.getPurchaseOrder().getId().equals(poId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "PO line " + poLine.getId() + " does not belong to Purchase Order " + poId);
        }
        if (poLine.getLineStatus() == LineStatus.CANCELLED || poLine.getLineStatus() == LineStatus.CLOSED || poLine.getLineStatus() == LineStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "PO line " + poLine.getLineNumber() + " is in invalid status: " + poLine.getLineStatus());
        }
    }

    private void updatePoShipmentStatus(PurchaseOrder po) {
        List<PurchaseOrderLineItem> activeLines = poLineRepository.findByPurchaseOrderIdAndDeletedFalseOrderByLineNumberAsc(po.getId());
        if (activeLines.isEmpty()) {
            return;
        }

        BigDecimal totalOrdered = BigDecimal.ZERO;
        BigDecimal totalShipped = BigDecimal.ZERO;

        for (PurchaseOrderLineItem line : activeLines) {
            if (line.getLineStatus() != LineStatus.CANCELLED && line.getLineStatus() != LineStatus.REJECTED) {
                totalOrdered = totalOrdered.add(line.getQuantityOrdered() != null ? line.getQuantityOrdered() : BigDecimal.ZERO);
                totalShipped = totalShipped.add(line.getShippedQuantity() != null ? line.getShippedQuantity() : BigDecimal.ZERO);
            }
        }

        if (totalShipped.compareTo(BigDecimal.ZERO) <= 0) {
            // No shipment active
            if (po.getPoStatus() == PoStatus.PARTIALLY_SHIPPED || po.getPoStatus() == PoStatus.FULLY_SHIPPED) {
                po.setPoStatus(PoStatus.SENT_TO_SUPPLIER);
            }
        } else if (totalShipped.compareTo(totalOrdered) >= 0) {
            po.setPoStatus(PoStatus.FULLY_SHIPPED);
        } else {
            po.setPoStatus(PoStatus.PARTIALLY_SHIPPED);
        }
    }

    private AdvanceShippingNotice findActiveOrThrow(Long id) {
        return asnRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Advance Shipping Notice not found with id: " + id));
    }

    private String generateAsnNumber() {
        return "ASN-" + System.currentTimeMillis();
    }

    private AsnLineRequestDto toCreateLineDto(AsnLineUpdateRequestDto updateDto) {
        AsnLineRequestDto createDto = new AsnLineRequestDto();
        createDto.setPoLineId(updateDto.getPoLineId());
        createDto.setShipmentQuantity(updateDto.getShipmentQuantity());
        createDto.setBatchNumber(updateDto.getBatchNumber());
        createDto.setSerialNumber(updateDto.getSerialNumber());
        createDto.setManufacturingDate(updateDto.getManufacturingDate());
        createDto.setExpiryDate(updateDto.getExpiryDate());
        createDto.setCountryOfOrigin(updateDto.getCountryOfOrigin());
        createDto.setWeight(updateDto.getWeight());
        createDto.setWeightUom(updateDto.getWeightUom());
        createDto.setVolume(updateDto.getVolume());
        createDto.setVolumeUom(updateDto.getVolumeUom());
        createDto.setNotes(updateDto.getNotes());
        return createDto;
    }

    private AsnResponseDto enrichAsnDto(AdvanceShippingNotice entity) {
        AsnResponseDto dto = asnMapper.toDto(entity);
        if (entity.getLines() != null) {
            List<AsnLineResponseDto> lineDtos = entity.getLines().stream()
                    .filter(l -> !l.isDeleted())
                    .map(asnLineMapper::toDto)
                    .toList();
            dto.setLines(lineDtos);
        }
        return dto;
    }

    private Specification<AdvanceShippingNotice> buildSpec(String search,
                                                           AsnStatus status,
                                                           Long purchaseOrderId,
                                                           String supplierId,
                                                           String warehouseId,
                                                           LocalDate startDate,
                                                           LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));

            if (status != null) {
                predicates.add(cb.equal(root.get("asnStatus"), status));
            }
            if (purchaseOrderId != null) {
                predicates.add(cb.equal(root.get("purchaseOrder").get("id"), purchaseOrderId));
            }
            if (supplierId != null && !supplierId.isBlank()) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("supplierId")), "%" + supplierId.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("supplierName")), "%" + supplierId.toLowerCase() + "%")
                ));
            }
            if (warehouseId != null && !warehouseId.isBlank()) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("warehouseId")), "%" + warehouseId.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("warehouseName")), "%" + warehouseId.toLowerCase() + "%")
                ));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("shipmentDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("shipmentDate"), endDate));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("asnNumber")), pattern),
                        cb.like(cb.lower(root.get("carrierName")), pattern),
                        cb.like(cb.lower(root.get("trackingNumber")), pattern),
                        cb.like(cb.lower(root.get("packingSlipNumber")), pattern),
                        cb.like(cb.lower(root.get("billOfLadingNumber")), pattern),
                        cb.like(cb.lower(root.get("containerNumber")), pattern),
                        cb.like(cb.lower(root.get("notes")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
