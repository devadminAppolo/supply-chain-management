package com.supplychain.outbound.packing.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderStatus;
import com.supplychain.outbound.outboundorder.domain.model.OutboundOrder;
import com.supplychain.outbound.outboundorder.infrastructure.repository.OutboundOrderRepository;
import com.supplychain.outbound.packing.application.dto.BulkAssignPackingRequestDto;
import com.supplychain.outbound.packing.application.dto.CompletePackingRequestDto;
import com.supplychain.outbound.packing.application.dto.PackingTaskResponseDto;
import com.supplychain.outbound.packing.domain.enums.PackingStatus;
import com.supplychain.outbound.packing.domain.model.PackingTask;
import com.supplychain.outbound.packing.infrastructure.mapper.PackingTaskMapper;
import com.supplychain.outbound.packing.infrastructure.repository.PackingTaskRepository;
import com.supplychain.wms.staff.domain.model.Staff;
import com.supplychain.wms.staff.infrastructure.repository.StaffRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackingServiceImpl implements PackingService {

    private final PackingTaskRepository  packingRepository;
    private final OutboundOrderRepository orderRepository;
    private final StaffRepository         staffRepository;
    private final PackingTaskMapper       mapper;

    // -------------------------------------------------------
    // AUTO-CREATION (called by PickingService)
    // -------------------------------------------------------

    @Override
    @Transactional
    public void createPackingTasksForOrder(OutboundOrder order, BigDecimal totalQty, String quantityUom) {
        log.info("Creating packing task for order={}, totalQty={}", order.getOrderNumber(), totalQty);

        // Guard: skip if packing tasks already exist (idempotency under retry/concurrency)
        long existing = packingRepository.countByOutboundOrderId(order.getId());
        if (existing > 0) {
            log.warn("Packing tasks already exist for order={}. Skipping creation.", order.getOrderNumber());
            return;
        }

        // One consolidated packing task per order
        PackingTask task = PackingTask.builder()
                .packingNumber(generatePackingNumber())
                .outboundOrderId(order.getId())
                .quantityToPack(totalQty)
                .quantityPacked(BigDecimal.ZERO)
                .quantityUom(quantityUom)
                .packingStatus(PackingStatus.PENDING)
                .orgId(order.getOrgId())
                .dcId(order.getDcId())
                .status("PENDING")
                .deleted(false)
                .build();

        packingRepository.save(task);
        log.info("Packing task {} created for order={}", task.getPackingNumber(), order.getOrderNumber());
    }

    // -------------------------------------------------------
    // READ
    // -------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PackingTaskResponseDto> findAll(String search,
                                                         PackingStatus status,
                                                         Long outboundOrderId,
                                                         String staffId,
                                                         Pageable pageable) {
        Specification<PackingTask> spec = buildSpec(search, status, outboundOrderId, staffId);
        return PageResponse.of(packingRepository.findAll(spec, pageable).map(this::enrich));
    }

    @Override
    @Transactional(readOnly = true)
    public PackingTaskResponseDto findById(Long id) {
        return enrich(findTaskOrThrow(id));
    }

    // -------------------------------------------------------
    // BULK ASSIGN
    // -------------------------------------------------------

    @Override
    @Transactional
    public List<PackingTaskResponseDto> bulkAssign(BulkAssignPackingRequestDto request) {
        log.info("Bulk assigning {} packing tasks to staff id={}",
                request.getPackingTaskIds().size(), request.getStaffId());

        Staff staff = staffRepository.findByIdAndDeletedFalse(request.getStaffId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Staff not found with id: " + request.getStaffId()));

        validateStaffActive(staff);

        // Validate all tasks first (all-or-nothing)
        List<PackingTask> tasks = new ArrayList<>();
        for (Long taskId : request.getPackingTaskIds()) {
            PackingTask task = findTaskOrThrow(taskId);
            if (task.getPackingStatus() != PackingStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Packing task " + task.getPackingNumber()
                                + " cannot be assigned from status: " + task.getPackingStatus()
                                + ". Only PENDING tasks can be assigned.");
            }
            tasks.add(task);
        }

        // Apply assignment
        List<PackingTaskResponseDto> results = new ArrayList<>();
        for (PackingTask task : tasks) {
            task.setAssignedStaffId(String.valueOf(staff.getId()));
            task.setAssignedStaffName(staff.getDisplayName());
            task.setPackingStatus(PackingStatus.ASSIGNED);
            task.setStatus("ASSIGNED");
            results.add(enrich(packingRepository.save(task)));
        }

        log.info("Assigned {} packing tasks to staff '{}'", tasks.size(), staff.getDisplayName());
        return results;
    }

    // -------------------------------------------------------
    // START PACKING
    // -------------------------------------------------------

    @Override
    @Transactional
    public PackingTaskResponseDto startPacking(Long id) {
        log.info("Starting Packing Task id={}", id);

        PackingTask task = findTaskOrThrow(id);

        if (task.getPackingStatus() == PackingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Packing task has already been completed: " + task.getPackingNumber());
        }
        if (task.getPackingStatus() == PackingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot start a CANCELLED packing task: " + task.getPackingNumber());
        }
        if (task.getPackingStatus() != PackingStatus.ASSIGNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Packing task must be ASSIGNED before it can be started. Current status: "
                            + task.getPackingStatus());
        }

        task.setPackingStatus(PackingStatus.IN_PROGRESS);
        task.setStatus("IN_PROGRESS");
        if (task.getStartedAt() == null) {
            task.setStartedAt(LocalDateTime.now());
        }

        PackingTask saved = packingRepository.save(task);
        log.info("Packing task {} started by staff '{}'",
                saved.getPackingNumber(), saved.getAssignedStaffName());
        return enrich(saved);
    }

    // -------------------------------------------------------
    // COMPLETE PACKING
    // -------------------------------------------------------

    @Override
    @Transactional
    public PackingTaskResponseDto completePacking(Long id, CompletePackingRequestDto request) {
        log.info("Completing Packing Task id={}, quantityPacked={}", id, request.getQuantityPacked());

        PackingTask task = findTaskOrThrow(id);

        // --- Status validation ---
        if (task.getPackingStatus() == PackingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Packing task has already been completed: " + task.getPackingNumber());
        }
        if (task.getPackingStatus() == PackingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot complete a CANCELLED packing task: " + task.getPackingNumber());
        }
        if (task.getPackingStatus() != PackingStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Packing task must be IN_PROGRESS to complete. Current status: "
                            + task.getPackingStatus());
        }

        // --- Quantity validation ---
        BigDecimal qtyPacked = request.getQuantityPacked();
        if (qtyPacked.compareTo(task.getQuantityToPack()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity packed (" + qtyPacked + ") cannot exceed quantity to pack ("
                            + task.getQuantityToPack() + ").");
        }

        // --- Finalize task ---
        task.setQuantityPacked(qtyPacked);
        task.setPackageType(request.getPackageType());
        task.setPackageId(generatePackageId());
        task.setWeight(request.getWeight());
        task.setWeightUom(request.getWeightUom());
        task.setVolume(request.getVolume());
        task.setVolumeUom(request.getVolumeUom());
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            task.setNotes(request.getNotes());
        }
        task.setPackingStatus(PackingStatus.COMPLETED);
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());

        PackingTask saved = packingRepository.save(task);
        log.info("Packing task {} COMPLETED — packageId={}, qty={}",
                saved.getPackingNumber(), saved.getPackageId(), qtyPacked);

        // --- Orchestrate: check if all packing tasks for the order are done ---
        checkAndAdvanceOrderAfterPacking(task.getOutboundOrderId());

        return enrich(saved);
    }

    // -------------------------------------------------------
    // POST-PACKING ORCHESTRATION
    // -------------------------------------------------------

    /**
     * After a packing task completes, acquires a pessimistic lock on the outbound order
     * and evaluates whether all active packing tasks are done. If so, transitions the
     * order to READY_TO_SHIP.
     */
    private void checkAndAdvanceOrderAfterPacking(Long orderId) {
        // Acquire exclusive row lock on the order before checking/transitioning status
        OutboundOrder order = orderRepository.findByIdAndDeletedFalseWithLock(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Outbound Order not found: " + orderId));

        // Guard: already transitioned
        OutboundOrderStatus current = order.getOrderStatus();
        if (current == OutboundOrderStatus.READY_TO_SHIP
                || current == OutboundOrderStatus.SHIPPED
                || current == OutboundOrderStatus.COMPLETED) {
            log.debug("Order {} already in status {}, skipping post-packing check", orderId, current);
            return;
        }

        List<PackingTask> allTasks = packingRepository.findAllByOutboundOrderIdAndDeletedFalse(orderId);

        // Consider only non-cancelled tasks
        List<PackingTask> activeTasks = allTasks.stream()
                .filter(t -> t.getPackingStatus() != PackingStatus.CANCELLED)
                .toList();

        if (activeTasks.isEmpty()) {
            log.warn("No active packing tasks found for order {}", orderId);
            return;
        }

        boolean allComplete = activeTasks.stream()
                .allMatch(t -> t.getPackingStatus() == PackingStatus.COMPLETED
                        && t.getQuantityPacked().compareTo(t.getQuantityToPack()) >= 0);

        if (!allComplete) {
            log.debug("Not all packing tasks done for order {}.", orderId);
            return;
        }

        // All packing complete → advance to READY_TO_SHIP
        log.info("All packing tasks COMPLETED for order={}. Transitioning to READY_TO_SHIP.",
                order.getOrderNumber());
        order.setOrderStatus(OutboundOrderStatus.READY_TO_SHIP);
        orderRepository.save(order);
        log.info("Order {} → READY_TO_SHIP.", order.getOrderNumber());
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    private String generatePackingNumber() {
        return "PACK-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String generatePackageId() {
        return "PKG-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private PackingTask findTaskOrThrow(Long id) {
        return packingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Packing task not found with id: " + id));
    }

    private void validateStaffActive(Staff staff) {
        if ("INACTIVE".equalsIgnoreCase(staff.getStatus())
                || "TERMINATED".equalsIgnoreCase(staff.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Staff member '" + staff.getDisplayName() + "' is not active.");
        }
    }

    private PackingTaskResponseDto enrich(PackingTask task) {
        PackingTaskResponseDto dto = mapper.toDto(task);

        // Compute derived field: quantity remaining
        if (task.getQuantityToPack() != null && task.getQuantityPacked() != null) {
            dto.setQuantityRemaining(task.getQuantityToPack().subtract(task.getQuantityPacked())
                    .max(BigDecimal.ZERO));
        }

        // Enrich order context
        orderRepository.findByIdAndDeletedFalse(task.getOutboundOrderId()).ifPresent(o -> {
            dto.setOrderNumber(o.getOrderNumber());
            dto.setCustomerCode(o.getCustomerCode());
            dto.setCustomerName(o.getCustomerName());
        });

        return dto;
    }

    private Specification<PackingTask> buildSpec(String search,
                                                  PackingStatus status,
                                                  Long outboundOrderId,
                                                  String staffId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (status != null) {
                predicates.add(cb.equal(root.get("packingStatus"), status));
            }
            if (outboundOrderId != null) {
                predicates.add(cb.equal(root.get("outboundOrderId"), outboundOrderId));
            }
            if (staffId != null && !staffId.isBlank()) {
                predicates.add(cb.equal(root.get("assignedStaffId"), staffId.trim()));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("packingNumber")), pattern),
                        cb.like(cb.lower(root.get("packageId")), pattern),
                        cb.like(cb.lower(root.get("assignedStaffName")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
