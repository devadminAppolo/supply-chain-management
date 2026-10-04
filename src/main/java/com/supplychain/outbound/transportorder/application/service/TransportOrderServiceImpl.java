package com.supplychain.outbound.transportorder.application.service;

import com.supplychain.catalog.domain.model.Sku;
import com.supplychain.catalog.infrastructure.repository.SkuRepository;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderStatus;
import com.supplychain.outbound.outboundorder.domain.model.OutboundOrder;
import com.supplychain.outbound.outboundorder.domain.model.OutboundOrderLine;
import com.supplychain.outbound.outboundorder.infrastructure.repository.OutboundOrderLineRepository;
import com.supplychain.outbound.outboundorder.infrastructure.repository.OutboundOrderRepository;
import com.supplychain.outbound.transportorder.application.dto.TransportOrderCreateRequestDto;
import com.supplychain.outbound.transportorder.application.dto.TransportOrderLineItemRequestDto;
import com.supplychain.outbound.transportorder.application.dto.TransportOrderResponseDto;
import com.supplychain.outbound.transportorder.application.dto.TransportOrderUpdateRequestDto;
import com.supplychain.outbound.transportorder.domain.enums.TransportOrderStatus;
import com.supplychain.outbound.transportorder.domain.model.TransportOrder;
import com.supplychain.outbound.transportorder.domain.model.TransportOrderLineItem;
import com.supplychain.outbound.transportorder.domain.model.TransportOrderOutboundOrder;
import com.supplychain.outbound.transportorder.infrastructure.mapper.TransportOrderMapper;
import com.supplychain.outbound.transportorder.infrastructure.repository.TransportOrderOutboundOrderRepository;
import com.supplychain.outbound.transportorder.infrastructure.repository.TransportOrderRepository;
import com.supplychain.tms.location.domain.model.Location;
import com.supplychain.tms.location.infrastructure.repository.LocationRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransportOrderServiceImpl implements TransportOrderService {

    private final TransportOrderRepository              repository;
    private final TransportOrderOutboundOrderRepository associationRepository;
    private final OutboundOrderRepository               outboundOrderRepository;
    private final OutboundOrderLineRepository           outboundOrderLineRepository;
    private final LocationRepository                    locationRepository;
    private final SkuRepository                         skuRepository;
    private final TransportOrderMapper                  mapper;

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    @Override
    @Transactional
    public TransportOrderResponseDto createTransportOrder(TransportOrderCreateRequestDto request) {
        boolean hasOutboundOrders = request.getOutboundOrderIds() != null && !request.getOutboundOrderIds().isEmpty();
        log.info("Creating Transport Order. Mode: {}", hasOutboundOrders ? "From Outbound Orders (" + request.getOutboundOrderIds().size() + ")" : "Manual");

        // 1. Validate pickup and delivery windows
        validateTimeWindow(request.getPickUpFrom(), request.getPickUpTo(), "pickup");
        validateTimeWindow(request.getDeliveryFrom(), request.getDeliveryTo(), "delivery");

        // 2. Validate and resolve destination location
        Location destination = validateAndGetLocation(request.getDestinationLocationId(), "Destination");

        Location origin;
        List<OutboundOrder> outboundOrders = null;

        if (hasOutboundOrders) {
            // Reject duplicate IDs
            validateNoDuplicateIds(request.getOutboundOrderIds());

            // Fetch and lock Outbound Orders
            outboundOrders = validateAndLockOutboundOrders(request.getOutboundOrderIds());

            // Validate origin compatibility – all selected OOs must share the same origin
            Long commonOriginId = resolveCommonOrigin(outboundOrders);
            origin = validateAndGetLocation(commonOriginId, "Origin");
        } else {
            // Manual creation mode
            if (request.getOriginLocationId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Origin location is required for manual Transport Order creation.");
            }
            origin = validateAndGetLocation(request.getOriginLocationId(), "Origin");
        }

        // 3. Origin and destination must not be the same location
        if (origin.getId().equals(destination.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Origin and destination locations must not be the same.");
        }

        // 4. Generate unique TO number (backend-authoritative, never client-supplied)
        String toNumber = generateToNumber();

        // 5. Build TO header
        TransportOrder transportOrder = TransportOrder.builder()
                .toNumber(toNumber)
                .toStatus(TransportOrderStatus.DRAFT)
                .mode(request.getMode().trim())
                .serviceLevelName(request.getServiceLevelName().trim())
                .notes(request.getNotes())
                .originLocationId(origin.getId())
                .originLocationCode(origin.getLocationCode())
                .originLocationName(origin.getLocationName())
                .destinationLocationId(destination.getId())
                .destinationLocationCode(destination.getLocationCode())
                .destinationLocationName(destination.getLocationName())
                .pickUpFrom(request.getPickUpFrom())
                .pickUpTo(request.getPickUpTo())
                .deliveryFrom(request.getDeliveryFrom())
                .deliveryTo(request.getDeliveryTo())
                .poNumber(request.getPoNumber())
                .asnNumber(request.getAsnNumber())
                .deleted(false)
                .build();

        // 6. Build line items and associations
        if (hasOutboundOrders) {
            int lineNumber = 1;
            for (OutboundOrder oo : outboundOrders) {
                // 6a. Build OO → TO association
                TransportOrderOutboundOrder assoc = TransportOrderOutboundOrder.builder()
                        .outboundOrderId(oo.getId())
                        .deleted(false)
                        .build();
                transportOrder.addOutboundOrderAssociation(assoc);

                // 6b. Build TO line items from each OO line
                List<OutboundOrderLine> ooLines =
                        outboundOrderLineRepository.findAllByOutboundOrderIdAndDeletedFalse(oo.getId());

                for (OutboundOrderLine ooLine : ooLines) {
                    TransportOrderLineItem item = buildLineItem(lineNumber, oo.getId(), ooLine);
                    transportOrder.addLineItem(item);
                    lineNumber++;
                }
            }
        } else if (request.getLineItems() != null && !request.getLineItems().isEmpty()) {
            int lineNumber = 1;
            for (TransportOrderLineItemRequestDto lineReq : request.getLineItems()) {
                TransportOrderLineItem item = buildManualLineItem(lineNumber, lineReq);
                transportOrder.addLineItem(item);
                lineNumber++;
            }
        }

        TransportOrder saved = repository.save(transportOrder);
        log.info("Created Transport Order id={}, toNumber={} with {} line(s) from {} OO(s)",
                saved.getId(), saved.getToNumber(),
                saved.getLineItems().size(),
                saved.getOutboundOrderAssociations().size());

        return mapper.toDto(saved);
    }

    // -------------------------------------------------------
    // READ
    // -------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TransportOrderResponseDto findById(Long id) {
        return mapper.toDto(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransportOrderResponseDto> findAll(String search,
                                                           TransportOrderStatus toStatus,
                                                           String mode,
                                                           LocalDate startDate,
                                                           LocalDate endDate,
                                                           Pageable pageable) {
        Specification<TransportOrder> spec = buildSpec(search, toStatus, mode, startDate, endDate);
        return PageResponse.of(repository.findAll(spec, pageable).map(mapper::toDto));
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------

    @Override
    @Transactional
    public TransportOrderResponseDto updateTransportOrder(Long id, TransportOrderUpdateRequestDto request) {
        log.info("Updating Transport Order id={}", id);

        TransportOrder to = findActiveOrThrow(id);
        assertEditable(to);

        // Validate windows
        validateTimeWindow(request.getPickUpFrom(), request.getPickUpTo(), "pickup");
        validateTimeWindow(request.getDeliveryFrom(), request.getDeliveryTo(), "delivery");

        // Validate and resolve new destination
        Location destination = validateAndGetLocation(request.getDestinationLocationId(), "Destination");

        // Origin is not editable after creation (it is determined by the associated OOs)
        if (destination.getId().equals(to.getOriginLocationId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Origin and destination locations must not be the same.");
        }

        // Apply mutable header fields
        to.setDestinationLocationId(destination.getId());
        to.setDestinationLocationCode(destination.getLocationCode());
        to.setDestinationLocationName(destination.getLocationName());
        to.setServiceLevelName(request.getServiceLevelName().trim());
        to.setMode(request.getMode().trim());
        to.setPickUpFrom(request.getPickUpFrom());
        to.setPickUpTo(request.getPickUpTo());
        to.setDeliveryFrom(request.getDeliveryFrom());
        to.setDeliveryTo(request.getDeliveryTo());
        to.setNotes(request.getNotes());
        to.setPoNumber(request.getPoNumber());
        to.setAsnNumber(request.getAsnNumber());

        TransportOrder saved = repository.save(to);
        log.info("Updated Transport Order id={}", saved.getId());

        return mapper.toDto(saved);
    }

    // -------------------------------------------------------
    // CANCEL
    // -------------------------------------------------------

    @Override
    @Transactional
    public TransportOrderResponseDto cancelTransportOrder(Long id) {
        log.info("Cancelling Transport Order id={}", id);

        TransportOrder to = findActiveOrThrow(id);

        if (to.getToStatus() == TransportOrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transport Order is already cancelled.");
        }
        if (to.getToStatus() == TransportOrderStatus.PLANNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transport Order cannot be cancelled from status: " + to.getToStatus()
                            + ". Only DRAFT and PENDING Transport Orders may be cancelled.");
        }

        to.setToStatus(TransportOrderStatus.CANCELLED);
        TransportOrder saved = repository.save(to);
        log.info("Cancelled Transport Order id={}", saved.getId());

        return mapper.toDto(saved);
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    private String generateToNumber() {
        return "TO-" + System.currentTimeMillis();
    }

    private TransportOrder findActiveOrThrow(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Transport Order not found with id: " + id));
    }

    private void assertEditable(TransportOrder to) {
        TransportOrderStatus s = to.getToStatus();
        if (s != TransportOrderStatus.DRAFT && s != TransportOrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transport Order cannot be modified in status: " + s
                            + ". Only DRAFT and PENDING Transport Orders are editable.");
        }
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

    private void validateTimeWindow(LocalDateTime start, LocalDateTime end, String windowName) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid " + windowName + " time window: end must be after start.");
        }
    }

    private void validateNoDuplicateIds(List<Long> ids) {
        Set<Long> seen = new HashSet<>();
        for (Long id : ids) {
            if (id == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Outbound Order ID list must not contain null values.");
            }
            if (!seen.add(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Duplicate Outbound Order ID in request: " + id);
            }
        }
    }

    /**
     * Fetches each Outbound Order with a PESSIMISTIC_WRITE lock and validates:
     * <ol>
     *   <li>The order exists and is not soft-deleted.</li>
     *   <li>The order is in READY_TO_SHIP status.</li>
     *   <li>The order is not already assigned to an active Transport Order.</li>
     * </ol>
     *
     * <p>Acquiring a pessimistic lock on each row prevents two concurrent requests
     * from simultaneously assigning the same order to different TOs.
     */
    private List<OutboundOrder> validateAndLockOutboundOrders(List<Long> ids) {
        List<OutboundOrder> result = new ArrayList<>();

        for (Long id : ids) {
            OutboundOrder oo = outboundOrderRepository.findByIdAndDeletedFalseWithLock(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Outbound Order not found with id: " + id));

            if (oo.getOrderStatus() != OutboundOrderStatus.READY_TO_SHIP) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Outbound Order id=" + id + " is not in READY_TO_SHIP status. "
                                + "Current status: " + oo.getOrderStatus());
            }

            associationRepository.findActiveAssociationByOutboundOrderId(id)
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Outbound Order id=" + id
                                        + " is already assigned to an active Transport Order. "
                                        + "Cancel that Transport Order before reassigning.");
                    });

            result.add(oo);
        }

        return result;
    }

    /**
     * Validates that all selected Outbound Orders share the same origin location.
     * Returns the common origin location ID.
     *
     * <p>Business rule: A Transport Order represents a single pickup movement;
     * all goods must be collected from the same origin.
     */
    private Long resolveCommonOrigin(List<OutboundOrder> outboundOrders) {
        Long commonOriginId = outboundOrders.get(0).getOriginLocationId();

        for (OutboundOrder oo : outboundOrders) {
            if (!commonOriginId.equals(oo.getOriginLocationId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "All selected Outbound Orders must have the same origin location. "
                                + "Outbound Order id=" + oo.getId()
                                + " has a different origin location id=" + oo.getOriginLocationId()
                                + " (expected id=" + commonOriginId + ").");
            }
        }

        return commonOriginId;
    }

    /**
     * Builds a Transport Order line item from a single Outbound Order Line.
     * Carries quantity, weight, volume, and SKU snapshots from the source line.
     */
    private TransportOrderLineItem buildLineItem(int lineNumber,
                                                  Long outboundOrderId,
                                                  OutboundOrderLine ooLine) {
        TransportOrderLineItem item = new TransportOrderLineItem();
        item.setLineNumber(lineNumber);
        item.setOutboundOrderId(outboundOrderId);
        item.setOutboundOrderLineId(ooLine.getId());
        item.setSkuId(ooLine.getSkuId());
        item.setSkuCode(ooLine.getSkuCode());
        item.setSkuName(ooLine.getSkuName());
        item.setQuantityOrdered(ooLine.getQuantityOrdered());
        item.setQuantityUom(ooLine.getQuantityUom());
        item.setTotalWeight(ooLine.getTotalWeight());
        item.setWeightUom(ooLine.getWeightUom() != null ? ooLine.getWeightUom() : "KG");
        item.setTotalVolume(ooLine.getTotalVolume());
        item.setVolumeUom(ooLine.getVolumeUom() != null ? ooLine.getVolumeUom() : "CBM");
        item.setDeleted(false);
        return item;
    }

    /**
     * Builds a Transport Order line item from a manual request line.
     */
    private TransportOrderLineItem buildManualLineItem(int lineNumber,
                                                        TransportOrderLineItemRequestDto lineReq) {
        TransportOrderLineItem item = new TransportOrderLineItem();
        item.setLineNumber(lineNumber);
        item.setQuantityOrdered(lineReq.getQuantityOrdered());
        item.setQuantityUom(lineReq.getQuantityUom() != null ? lineReq.getQuantityUom() : "EA");
        item.setTotalWeight(lineReq.getTotalWeight());
        item.setWeightUom(lineReq.getWeightUom() != null ? lineReq.getWeightUom() : "KG");
        item.setTotalVolume(lineReq.getTotalVolume());
        item.setVolumeUom(lineReq.getVolumeUom() != null ? lineReq.getVolumeUom() : "CBM");
        item.setOutboundOrderId(lineReq.getOutboundOrderId());
        item.setOutboundOrderLineId(lineReq.getOutboundOrderLineId());
        item.setDeleted(false);

        if (lineReq.getSkuId() != null) {
            Sku sku = skuRepository.findById(lineReq.getSkuId()).orElse(null);
            if (sku != null) {
                item.setSkuId(sku.getId());
                item.setSkuCode(sku.getSkuCode());
                item.setSkuName(lineReq.getSkuName() != null && !lineReq.getSkuName().isBlank()
                        ? lineReq.getSkuName()
                        : sku.getSkuCode());
            } else {
                item.setSkuId(lineReq.getSkuId());
                item.setSkuCode(lineReq.getSkuCode());
                item.setSkuName(lineReq.getSkuName() != null ? lineReq.getSkuName() : lineReq.getSkuCode());
            }
        } else {
            item.setSkuCode(lineReq.getSkuCode());
            item.setSkuName(lineReq.getSkuName() != null ? lineReq.getSkuName() : lineReq.getSkuCode());
        }

        return item;
    }

    // -------------------------------------------------------
    // JPA Specification builder
    // -------------------------------------------------------

    private Specification<TransportOrder> buildSpec(String search,
                                                     TransportOrderStatus toStatus,
                                                     String mode,
                                                     LocalDate startDate,
                                                     LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleted")));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("toNumber")), pattern));
            }

            if (toStatus != null) {
                predicates.add(cb.equal(root.get("toStatus"), toStatus));
            }

            if (mode != null && !mode.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("mode")),
                        "%" + mode.trim().toLowerCase() + "%"));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("pickUpFrom"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(cb.lessThan(
                        root.get("pickUpFrom"), endDate.plusDays(1).atStartOfDay()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
