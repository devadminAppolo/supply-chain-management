package com.supplychain.outbound.outboundorder.application.service;

import com.supplychain.catalog.domain.model.Product;
import com.supplychain.catalog.domain.model.Sku;
import com.supplychain.catalog.infrastructure.repository.ProductRepository;
import com.supplychain.catalog.infrastructure.repository.SkuRepository;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.customer.domain.enums.CustomerStatus;
import com.supplychain.customer.domain.model.Customer;
import com.supplychain.customer.application.service.CustomerService;
import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderCreateRequestDto;
import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderLineRequestDto;
import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderLineUpdateRequestDto;
import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderResponseDto;
import com.supplychain.outbound.outboundorder.application.dto.OutboundOrderUpdateRequestDto;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderStatus;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderType;
import com.supplychain.outbound.outboundorder.domain.model.OutboundOrder;
import com.supplychain.outbound.outboundorder.domain.model.OutboundOrderLine;
import com.supplychain.outbound.outboundorder.infrastructure.mapper.OutboundOrderLineMapper;
import com.supplychain.outbound.outboundorder.infrastructure.mapper.OutboundOrderMapper;
import com.supplychain.outbound.outboundorder.infrastructure.repository.OutboundOrderRepository;
import com.supplychain.tms.location.domain.model.Location;
import com.supplychain.tms.location.infrastructure.repository.LocationRepository;
import jakarta.persistence.criteria.Predicate;
import com.supplychain.outbound.picking.application.service.PickingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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
public class OutboundOrderServiceImpl implements OutboundOrderService {

    private final OutboundOrderRepository     repository;
    private final LocationRepository          locationRepository;
    private final SkuRepository               skuRepository;
    private final ProductRepository           productRepository;
    private final OutboundOrderMapper         mapper;
    private final OutboundOrderLineMapper     lineMapper;
    private final CustomerService             customerService;

    // @Lazy to break circular dependency:
    // OutboundOrderServiceImpl → PickingService → PackingService
    @Lazy
    private final PickingService pickingService;

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    @Override
    @Transactional
    public OutboundOrderResponseDto createOutboundOrder(OutboundOrderCreateRequestDto request) {
        log.info("Creating Outbound Order for customerId: {}", request.getCustomerId());

        // 1. Validate time windows
        validateTimeWindows(request.getPickTimeWindowStart(),   request.getPickTimeWindowEnd(),   "pick");
        validateTimeWindows(request.getDeliveryTimeWindowStart(), request.getDeliveryTimeWindowEnd(), "delivery");

        // 2. Validate and resolve customer from Customer Master
        Customer customer = validateAndGetActiveCustomer(request.getCustomerId());

        // 3. Validate and resolve origin location
        Location origin = validateAndGetLocation(request.getOriginLocationId(), "Origin");

        // 4. Validate and resolve destination location
        Location destination = validateAndGetLocation(request.getDestinationLocationId(), "Destination");

        // 5. Generate order number (backend-authoritative, never client-supplied)
        String orderNumber = generateOrderNumber();

        // 6. Build header entity
        OutboundOrder order = OutboundOrder.builder()
                .orderNumber(orderNumber)
                .orderDate(request.getOrderDate())
                .orderType(request.getOrderType())
                .orderStatus(OutboundOrderStatus.DRAFT)
                .customerId(customer.getId())
                .customerCode(customer.getCustomerCode())
                .customerName(customer.getCustomerName())
                .originLocationId(origin.getId())
                .originLocationCode(origin.getLocationCode())
                .originLocationName(origin.getLocationName())
                .destinationLocationId(destination.getId())
                .destinationLocationCode(destination.getLocationCode())
                .destinationLocationName(destination.getLocationName())
                .pickTimeWindowStart(request.getPickTimeWindowStart())
                .pickTimeWindowEnd(request.getPickTimeWindowEnd())
                .deliveryTimeWindowStart(request.getDeliveryTimeWindowStart())
                .deliveryTimeWindowEnd(request.getDeliveryTimeWindowEnd())
                .currencyCode(request.getCurrencyCode())
                .instructions(request.getInstructions())
                .notes(request.getNotes())
                .deleted(false)
                .build();

        // 6. Process lines: validate items, compute prices/weights/volumes
        BigDecimal orderTotal = BigDecimal.ZERO;
        int lineNumber = 1;
        for (OutboundOrderLineRequestDto lineRequest : request.getLines()) {
            Sku sku = validateAndGetSku(lineRequest.getSkuId(), lineNumber);
            Product product = resolveProduct(sku);

            OutboundOrderLine line = buildLine(lineNumber, sku, product, lineRequest);
            order.addLine(line);
            orderTotal = orderTotal.add(line.getTotalPrice() != null ? line.getTotalPrice() : BigDecimal.ZERO);
            lineNumber++;
        }

        order.setTotalAmount(orderTotal);

        OutboundOrder saved = repository.save(order);
        log.info("Created Outbound Order id={}, orderNumber={} with {} lines",
                saved.getId(), saved.getOrderNumber(), saved.getLines().size());

        return mapper.toDto(saved);
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------

    @Override
    @Transactional
    public OutboundOrderResponseDto updateOutboundOrder(Long id, OutboundOrderUpdateRequestDto request) {
        log.info("Updating Outbound Order id={}", id);

        OutboundOrder order = findActiveOrThrow(id);
        assertEditable(order);

        // Validate time windows
        validateTimeWindows(request.getPickTimeWindowStart(),    request.getPickTimeWindowEnd(),    "pick");
        validateTimeWindows(request.getDeliveryTimeWindowStart(), request.getDeliveryTimeWindowEnd(), "delivery");

        // Validate and resolve customer from Customer Master
        Customer customer = validateAndGetActiveCustomer(request.getCustomerId());

        // Validate/resolve locations (only update snapshots if location changed)
        Location origin      = validateAndGetLocation(request.getOriginLocationId(),      "Origin");
        Location destination = validateAndGetLocation(request.getDestinationLocationId(), "Destination");

        // Apply mutable header fields (mapper ignores customerId, customerCode, customerName)
        mapper.updateEntity(request, order);

        // Refresh customer reference and snapshots
        order.setCustomerId(customer.getId());
        order.setCustomerCode(customer.getCustomerCode());
        order.setCustomerName(customer.getCustomerName());

        // Refresh location snapshots (mapper ignores them deliberately)
        order.setOriginLocationId(origin.getId());
        order.setOriginLocationCode(origin.getLocationCode());
        order.setOriginLocationName(origin.getLocationName());
        order.setDestinationLocationId(destination.getId());
        order.setDestinationLocationCode(destination.getLocationCode());
        order.setDestinationLocationName(destination.getLocationName());

        // Synchronise child lines (add / update / remove orphans)
        Map<Long, OutboundOrderLine> existingLineMap = order.getLines().stream()
                .filter(l -> l.getId() != null)
                .collect(Collectors.toMap(OutboundOrderLine::getId, Function.identity()));

        List<OutboundOrderLine> updatedLines = new ArrayList<>();
        Set<Long> processedIds  = new HashSet<>();
        BigDecimal orderTotal   = BigDecimal.ZERO;
        int lineNumber          = 1;

        for (OutboundOrderLineUpdateRequestDto lineRequest : request.getLines()) {
            Sku     sku     = validateAndGetSku(lineRequest.getSkuId(), lineNumber);
            Product product = resolveProduct(sku);

            if (lineRequest.getId() != null && existingLineMap.containsKey(lineRequest.getId())) {
                // Update existing line
                OutboundOrderLine existing = existingLineMap.get(lineRequest.getId());
                lineMapper.updateLineEntity(lineRequest, existing);
                applySkuSnapshot(existing, sku, product);
                applyCalculatedFields(existing, sku, product, lineRequest.getQuantityOrdered(), lineRequest.getUnitPrice());
                existing.setLineNumber(lineNumber);
                existing.setDeleted(false);
                processedIds.add(lineRequest.getId());
                updatedLines.add(existing);
            } else {
                // New line
                OutboundOrderLine newLine = buildLine(lineNumber, sku, product,
                        toCreateDto(lineRequest));
                newLine.setOutboundOrder(order);
                updatedLines.add(newLine);
            }

            OutboundOrderLine currentLine = updatedLines.get(updatedLines.size() - 1);
            orderTotal = orderTotal.add(currentLine.getTotalPrice() != null ? currentLine.getTotalPrice() : BigDecimal.ZERO);
            lineNumber++;
        }

        // Remove orphan lines (those not present in the updated request)
        List<OutboundOrderLine> toRemove = order.getLines().stream()
                .filter(l -> l.getId() != null && !processedIds.contains(l.getId()))
                .toList();
        toRemove.forEach(order::removeLine);

        // Reset and repopulate the managed collection
        order.getLines().clear();
        updatedLines.forEach(order::addLine);

        order.setTotalAmount(orderTotal);

        OutboundOrder saved = repository.save(order);
        log.info("Updated Outbound Order id={}", saved.getId());

        return mapper.toDto(saved);
    }

    // -------------------------------------------------------
    // READ
    // -------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public OutboundOrderResponseDto findById(Long id) {
        return mapper.toDto(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OutboundOrderResponseDto> findAll(String search,
                                                          OutboundOrderStatus orderStatus,
                                                          OutboundOrderType orderType,
                                                          String customerCode,
                                                          LocalDate startDate,
                                                          LocalDate endDate,
                                                          Pageable pageable) {
        Specification<OutboundOrder> spec = buildSpec(search, orderStatus, orderType, customerCode, startDate, endDate);
        return PageResponse.of(repository.findAll(spec, pageable).map(mapper::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DropdownOptionDto> findDropdown() {
        return repository.findAllByDeletedFalseOrderByOrderNumberAsc()
                .stream()
                .map(o -> new DropdownOptionDto(o.getOrderNumber(), o.getOrderNumber()))
                .distinct()
                .toList();
    }

    // -------------------------------------------------------
    // CONFIRM
    // -------------------------------------------------------

    @Override
    @Transactional
    public OutboundOrderResponseDto confirmOutboundOrder(Long id) {
        log.info("Confirming Outbound Order id={}", id);

        OutboundOrder order = findActiveOrThrow(id);

        if (order.getOrderStatus() != OutboundOrderStatus.DRAFT
                && order.getOrderStatus() != OutboundOrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Outbound Order cannot be confirmed from status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OutboundOrderStatus.CONFIRMED);
        OutboundOrder saved = repository.save(order);
        log.info("Confirmed Outbound Order id={}. Triggering inventory allocation.", saved.getId());

        // Allocate inventory and create pick tasks; advances order to PICKING on success
        pickingService.allocateAndCreatePickTasks(saved.getId());

        // Re-fetch to return the current status (PICKING after successful allocation)
        return mapper.toDto(findActiveOrThrow(saved.getId()));
    }

    // -------------------------------------------------------
    // CANCEL
    // -------------------------------------------------------

    @Override
    @Transactional
    public OutboundOrderResponseDto cancelOutboundOrder(Long id) {
        log.info("Cancelling Outbound Order id={}", id);

        OutboundOrder order = findActiveOrThrow(id);

        OutboundOrderStatus current = order.getOrderStatus();
        if (current == OutboundOrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Outbound Order is already cancelled.");
        }
        if (current == OutboundOrderStatus.SHIPPED || current == OutboundOrderStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Outbound Order cannot be cancelled from status: " + current);
        }

        order.setOrderStatus(OutboundOrderStatus.CANCELLED);
        OutboundOrder saved = repository.save(order);
        log.info("Cancelled Outbound Order id={}", saved.getId());

        return mapper.toDto(saved);
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------

    @Override
    @Transactional
    public void deleteById(Long id) {
        OutboundOrder order = findActiveOrThrow(id);

        if (order.getOrderStatus() != OutboundOrderStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only DRAFT orders can be deleted. Cancel the order first.");
        }

        softDelete(order);
        repository.save(order);
        log.info("Soft-deleted Outbound Order id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        for (Long id : ids) {
            OutboundOrder order = findActiveOrThrow(id);
            if (order.getOrderStatus() != OutboundOrderStatus.DRAFT) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Only DRAFT orders can be deleted. Order id=" + id
                                + " has status " + order.getOrderStatus());
            }
            softDelete(order);
        }
        log.info("Soft-deleted {} Outbound Orders", ids.size());
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    private String generateOrderNumber() {
        return "ORDER-" + System.currentTimeMillis();
    }

    private OutboundOrder findActiveOrThrow(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Outbound Order not found with id: " + id));
    }

    private Customer validateAndGetActiveCustomer(Long customerId) {
        Customer customer = customerService.getActiveEntityById(customerId);
        if (!CustomerStatus.ACTIVE.name().equalsIgnoreCase(customer.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Customer is not active: " + customer.getCustomerCode());
        }
        return customer;
    }

    private Location validateAndGetLocation(Long locationId, String role) {
        Location location = locationRepository.findById(locationId)
                .filter(l -> !l.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        role + " location not found with id: " + locationId));

        if (!location.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    role + " location is not active: " + location.getLocationCode());
        }

        return location;
    }

    private Sku validateAndGetSku(Long skuId, int lineNumber) {
        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "SKU not found with id: " + skuId + " (line " + lineNumber + ")"));

        if (sku.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "SKU is inactive: " + sku.getSkuCode() + " (line " + lineNumber + ")");
        }

        String skuStatus = sku.getStatus();
        if (skuStatus != null && !"ACTIVE".equalsIgnoreCase(skuStatus)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "SKU is not active: " + sku.getSkuCode() + " (line " + lineNumber + ")");
        }

        return sku;
    }

    private Product resolveProduct(Sku sku) {
        if (sku.getProductId() == null) {
            return null;
        }
        return productRepository.findById(sku.getProductId()).orElse(null);
    }

    private OutboundOrderLine buildLine(int lineNumber,
                                        Sku sku,
                                        Product product,
                                        OutboundOrderLineRequestDto lineRequest) {
        OutboundOrderLine line = new OutboundOrderLine();
        line.setLineNumber(lineNumber);
        line.setSkuId(sku.getId());
        line.setSkuCode(sku.getSkuCode());
        line.setSkuName(product != null ? product.getProductName() : sku.getSkuCode());
        line.setQuantityOrdered(lineRequest.getQuantityOrdered());
        line.setQuantityUom(lineRequest.getQuantityUom());
        line.setUnitPrice(lineRequest.getUnitPrice());
        line.setCurrencyCode(lineRequest.getCurrencyCode());
        line.setDeleted(false);

        // Backend-calculated: totalPrice = quantityOrdered × unitPrice
        applyPriceCalculation(line);

        // Backend-calculated: totalWeight = sku.weight × quantityOrdered
        applyWeightCalculation(line, sku);

        // Backend-calculated: totalVolume = product.volume × quantityOrdered
        applyVolumeCalculation(line, product);

        return line;
    }

    private void applySkuSnapshot(OutboundOrderLine line, Sku sku, Product product) {
        line.setSkuId(sku.getId());
        line.setSkuCode(sku.getSkuCode());
        line.setSkuName(product != null ? product.getProductName() : sku.getSkuCode());
    }

    private void applyCalculatedFields(OutboundOrderLine line,
                                        Sku sku,
                                        Product product,
                                        BigDecimal quantity,
                                        BigDecimal unitPrice) {
        line.setQuantityOrdered(quantity);
        line.setUnitPrice(unitPrice);
        applyPriceCalculation(line);
        applyWeightCalculation(line, sku);
        applyVolumeCalculation(line, product);
    }

    private void applyPriceCalculation(OutboundOrderLine line) {
        if (line.getUnitPrice() != null && line.getQuantityOrdered() != null) {
            line.setTotalPrice(line.getUnitPrice().multiply(line.getQuantityOrdered()));
        } else {
            line.setTotalPrice(BigDecimal.ZERO);
        }
    }

    private void applyWeightCalculation(OutboundOrderLine line, Sku sku) {
        if (sku.getWeight() != null && line.getQuantityOrdered() != null) {
            BigDecimal weight = BigDecimal.valueOf(sku.getWeight())
                    .multiply(line.getQuantityOrdered());
            line.setTotalWeight(weight);
            line.setWeightUom("KG");
        }
    }

    private void applyVolumeCalculation(OutboundOrderLine line, Product product) {
        if (product != null && product.getVolume() != null && line.getQuantityOrdered() != null) {
            BigDecimal volume = BigDecimal.valueOf(product.getVolume())
                    .multiply(line.getQuantityOrdered());
            line.setTotalVolume(volume);
            line.setVolumeUom("CBM");
        }
    }

    private void validateTimeWindows(java.time.LocalDateTime start,
                                      java.time.LocalDateTime end,
                                      String windowName) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid " + windowName + " time window: end must be after start.");
        }
    }

    private void assertEditable(OutboundOrder order) {
        OutboundOrderStatus s = order.getOrderStatus();
        if (s != OutboundOrderStatus.DRAFT && s != OutboundOrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Outbound Order cannot be modified in status: " + s
                            + ". Only DRAFT and PENDING orders are editable.");
        }
    }

    private void softDelete(OutboundOrder order) {
        order.setDeleted(true);
        if (order.getLines() != null) {
            order.getLines().forEach(l -> l.setDeleted(true));
        }
    }

    private OutboundOrderLineRequestDto toCreateDto(OutboundOrderLineUpdateRequestDto src) {
        OutboundOrderLineRequestDto dto = new OutboundOrderLineRequestDto();
        dto.setSkuId(src.getSkuId());
        dto.setQuantityOrdered(src.getQuantityOrdered());
        dto.setQuantityUom(src.getQuantityUom());
        dto.setUnitPrice(src.getUnitPrice());
        dto.setCurrencyCode(src.getCurrencyCode());
        return dto;
    }

    // -------------------------------------------------------
    // JPA Specification builder for findAll
    // -------------------------------------------------------

    private Specification<OutboundOrder> buildSpec(String search,
                                                    OutboundOrderStatus orderStatus,
                                                    OutboundOrderType orderType,
                                                    String customerCode,
                                                    LocalDate startDate,
                                                    LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleted")));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("orderNumber")), pattern),
                        cb.like(cb.lower(root.get("customerCode")), pattern),
                        cb.like(cb.lower(root.get("customerName")), pattern)
                ));
            }

            if (orderStatus != null) {
                predicates.add(cb.equal(root.get("orderStatus"), orderStatus));
            }

            if (orderType != null) {
                predicates.add(cb.equal(root.get("orderType"), orderType));
            }

            if (customerCode != null && !customerCode.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("customerCode")),
                        "%" + customerCode.trim().toLowerCase() + "%"));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
