package com.supplychain.outbound.picking.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.InventoryStatus;
import com.supplychain.common.domain.enums.TransactionType;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderStatus;
import com.supplychain.outbound.outboundorder.domain.model.OutboundOrder;
import com.supplychain.outbound.outboundorder.domain.model.OutboundOrderLine;
import com.supplychain.outbound.outboundorder.infrastructure.repository.OutboundOrderLineRepository;
import com.supplychain.outbound.outboundorder.infrastructure.repository.OutboundOrderRepository;
import com.supplychain.outbound.packing.application.service.PackingService;
import com.supplychain.outbound.picking.application.dto.BulkAssignPickingRequestDto;
import com.supplychain.outbound.picking.application.dto.CompletePickingRequestDto;
import com.supplychain.outbound.picking.application.dto.PickingTaskResponseDto;
import com.supplychain.outbound.picking.domain.enums.PickingStatus;
import com.supplychain.outbound.picking.domain.model.PickingTask;
import com.supplychain.outbound.picking.infrastructure.mapper.PickingTaskMapper;
import com.supplychain.outbound.picking.infrastructure.repository.PickingTaskRepository;
import com.supplychain.wms.inventory.domain.model.Inventory;
import com.supplychain.wms.inventory.infrastructure.repository.InventoryRepository;
import com.supplychain.wms.inventorytransaction.domain.model.InventoryTransaction;
import com.supplychain.wms.inventorytransaction.infrastructure.repository.InventoryTransactionRepository;
import com.supplychain.wms.location.domain.model.Bin;
import com.supplychain.wms.location.infrastructure.repository.BinRepository;
import com.supplychain.wms.staff.domain.model.Staff;
import com.supplychain.wms.staff.infrastructure.repository.StaffRepository;
import jakarta.persistence.criteria.Predicate;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PickingServiceImpl implements PickingService {

    private final PickingTaskRepository           pickingRepository;
    private final OutboundOrderRepository         orderRepository;
    private final OutboundOrderLineRepository     orderLineRepository;
    private final InventoryRepository             inventoryRepository;
    private final InventoryTransactionRepository  transactionRepository;
    private final BinRepository                   binRepository;
    private final StaffRepository                 staffRepository;
    private final PickingTaskMapper               mapper;

    // @Lazy to avoid Spring circular-dependency chain:
    // PickingServiceImpl → PackingService → (no back-reference to Picking)
    @Lazy
    private final PackingService packingService;

    // -------------------------------------------------------
    // ALLOCATION & PICK TASK CREATION
    // -------------------------------------------------------

    @Override
    @Transactional
    public void allocateAndCreatePickTasks(Long orderId) {
        log.info("Starting inventory allocation for Outbound Order id={}", orderId);

        OutboundOrder order = findOrderOrThrow(orderId);

        List<OutboundOrderLine> lines =
                orderLineRepository.findAllByOutboundOrderIdAndDeletedFalse(orderId);

        if (lines.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Outbound Order has no lines to allocate: " + order.getOrderNumber());
        }

        List<PickingTask> createdTasks = new ArrayList<>();

        for (OutboundOrderLine line : lines) {
            List<PickingTask> lineTasks = allocateLine(order, line);
            createdTasks.addAll(lineTasks);
        }

        if (createdTasks.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No available inventory found for any line of order: " + order.getOrderNumber()
                            + ". Ensure stock is received and available before confirming.");
        }

        order.setOrderStatus(OutboundOrderStatus.PICKING);
        orderRepository.save(order);

        log.info("Allocation complete for order={}. Created {} pick task(s). Order status → PICKING",
                order.getOrderNumber(), createdTasks.size());
    }

    /** Allocates inventory for a single order line (FIFO by inventory ID). */
    private List<PickingTask> allocateLine(OutboundOrder order, OutboundOrderLine line) {
        BigDecimal remaining = line.getQuantityOrdered();
        List<PickingTask> tasks = new ArrayList<>();

        // Pessimistic-write locked query – prevents concurrent double-allocation
        List<Inventory> available =
                inventoryRepository.findAvailableInventoryBySkuForUpdate(line.getSkuId());

        for (Inventory inv : available) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal allocQty = remaining.min(inv.getAvailableQuantity());

            // Increment allocatedQuantity, decrement availableQuantity
            inv.setAllocatedQuantity(inv.getAllocatedQuantity().add(allocQty));
            inv.setAvailableQuantity(inv.getAvailableQuantity().subtract(allocQty));
            inventoryRepository.save(inv);

            String binCode = binRepository.findById(inv.getBinId())
                    .map(Bin::getBinCode).orElse("UNKNOWN");

            PickingTask task = PickingTask.builder()
                    .pickNumber(generatePickNumber())
                    .outboundOrderId(order.getId())
                    .outboundOrderLineId(line.getId())
                    .skuId(line.getSkuId())
                    .skuCode(line.getSkuCode())
                    .skuName(line.getSkuName())
                    .inventoryId(inv.getId())
                    .warehouseId(inv.getWarehouseId())
                    .sourceBinId(inv.getBinId())
                    .sourceBinCode(binCode)
                    .quantityToPick(allocQty)
                    .quantityPicked(BigDecimal.ZERO)
                    .quantityUom(line.getQuantityUom())
                    .pickingStatus(PickingStatus.PENDING)
                    .status("PENDING")
                    .deleted(false)
                    .build();

            tasks.add(pickingRepository.save(task));
            remaining = remaining.subtract(allocQty);

            log.debug("Pick task created: pickNumber={}, bin={}, qty={}",
                    task.getPickNumber(), binCode, allocQty);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("Partial allocation for order={}, line={}, skuId={} — unallocated qty={}",
                    order.getOrderNumber(), line.getLineNumber(), line.getSkuId(), remaining);
        }

        return tasks;
    }

    // -------------------------------------------------------
    // READ
    // -------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PickingTaskResponseDto> findAll(String search,
                                                         PickingStatus status,
                                                         Long outboundOrderId,
                                                         String staffId,
                                                         Pageable pageable) {
        Specification<PickingTask> spec = buildSpec(search, status, outboundOrderId, staffId);
        return PageResponse.of(pickingRepository.findAll(spec, pageable).map(this::enrich));
    }

    @Override
    @Transactional(readOnly = true)
    public PickingTaskResponseDto findById(Long id) {
        return enrich(findTaskOrThrow(id));
    }

    // -------------------------------------------------------
    // BULK ASSIGN
    // -------------------------------------------------------

    @Override
    @Transactional
    public List<PickingTaskResponseDto> bulkAssign(BulkAssignPickingRequestDto request) {
        log.info("Bulk assigning {} pick tasks to staff id={}",
                request.getPickTaskIds().size(), request.getStaffId());

        Staff staff = staffRepository.findByIdAndDeletedFalse(request.getStaffId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Staff not found with id: " + request.getStaffId()));

        validateStaffActive(staff);

        // Validate all tasks first (all-or-nothing)
        List<PickingTask> tasks = new ArrayList<>();
        for (Long taskId : request.getPickTaskIds()) {
            PickingTask task = findTaskOrThrow(taskId);
            if (task.getPickingStatus() != PickingStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Pick task " + task.getPickNumber()
                                + " cannot be assigned from status: " + task.getPickingStatus()
                                + ". Only PENDING tasks can be assigned.");
            }
            tasks.add(task);
        }

        // Apply assignment
        List<PickingTaskResponseDto> results = new ArrayList<>();
        for (PickingTask task : tasks) {
            task.setAssignedStaffId(String.valueOf(staff.getId()));
            task.setAssignedStaffName(staff.getDisplayName());
            task.setPickingStatus(PickingStatus.ASSIGNED);
            task.setStatus("ASSIGNED");
            results.add(enrich(pickingRepository.save(task)));
        }

        log.info("Assigned {} pick tasks to staff '{}'", tasks.size(), staff.getDisplayName());
        return results;
    }

    // -------------------------------------------------------
    // START PICKING
    // -------------------------------------------------------

    @Override
    @Transactional
    public PickingTaskResponseDto startPicking(Long id) {
        log.info("Starting Pick Task id={}", id);

        PickingTask task = findTaskOrThrow(id);

        if (task.getPickingStatus() == PickingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pick task has already been completed: " + task.getPickNumber());
        }
        if (task.getPickingStatus() == PickingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot start a CANCELLED pick task: " + task.getPickNumber());
        }
        if (task.getPickingStatus() != PickingStatus.ASSIGNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pick task must be ASSIGNED before it can be started. Current status: "
                            + task.getPickingStatus());
        }

        task.setPickingStatus(PickingStatus.IN_PROGRESS);
        task.setStatus("IN_PROGRESS");
        if (task.getStartedAt() == null) {
            task.setStartedAt(LocalDateTime.now());
        }

        PickingTask saved = pickingRepository.save(task);
        log.info("Pick task {} started by staff '{}'", saved.getPickNumber(), saved.getAssignedStaffName());
        return enrich(saved);
    }

    // -------------------------------------------------------
    // COMPLETE PICKING
    // -------------------------------------------------------

    @Override
    @Transactional
    public PickingTaskResponseDto completePicking(Long id, CompletePickingRequestDto request) {
        log.info("Completing Pick Task id={}, quantityPicked={}", id, request.getQuantityPicked());

        PickingTask task = findTaskOrThrow(id);

        // --- Status validation ---
        if (task.getPickingStatus() == PickingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pick task has already been completed: " + task.getPickNumber());
        }
        if (task.getPickingStatus() == PickingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot complete a CANCELLED pick task: " + task.getPickNumber());
        }
        if (task.getPickingStatus() != PickingStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pick task must be IN_PROGRESS to complete. Current status: "
                            + task.getPickingStatus());
        }

        // --- Quantity validation ---
        BigDecimal qtyPicked = request.getQuantityPicked();
        if (qtyPicked.compareTo(task.getQuantityToPick()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity picked (" + qtyPicked + ") cannot exceed quantity to pick ("
                            + task.getQuantityToPick() + ").");
        }

        // --- Inventory update (pessimistic lock via findInventoryByKey) ---
        Inventory inv = locateInventoryForPick(task);

        BigDecimal qtyBefore = inv.getOnHandQuantity();

        // Item physically leaves the bin: decrement both allocatedQty and onHandQty
        BigDecimal newAllocated = inv.getAllocatedQuantity().subtract(qtyPicked).max(BigDecimal.ZERO);
        BigDecimal newOnHand    = inv.getOnHandQuantity().subtract(qtyPicked).max(BigDecimal.ZERO);
        BigDecimal newAvailable = newOnHand
                .subtract(inv.getReservedQuantity() != null ? inv.getReservedQuantity() : BigDecimal.ZERO)
                .subtract(newAllocated)
                .max(BigDecimal.ZERO);

        inv.setAllocatedQuantity(newAllocated);
        inv.setOnHandQuantity(newOnHand);
        inv.setAvailableQuantity(newAvailable);
        inventoryRepository.save(inv);

        // --- Record inventory transaction ---
        InventoryTransaction txn = InventoryTransaction.builder()
                .orgId(task.getOrgId())
                .dcId(task.getDcId())
                .warehouseId(inv.getWarehouseId())
                .inventoryId(inv.getId())
                .productId(inv.getProductId())
                .skuId(inv.getSkuId())
                .uomId(inv.getUomId() != null ? inv.getUomId() : 1L)
                .fromBinId(inv.getBinId())
                .transactionType(TransactionType.PICKING)
                .quantity(qtyPicked)
                .lotNumber(inv.getLotNumber())
                .serialNumber(inv.getSerialNumber())
                .referenceType("PICKING_TASK")
                .referenceId(task.getPickNumber())
                .reasonCode("PICKING_COMPLETED")
                .transactionDate(LocalDateTime.now())
                .quantityBefore(qtyBefore)
                .quantityAfter(inv.getOnHandQuantity())
                .deleted(false)
                .build();
        transactionRepository.save(txn);

        // --- Finalize task ---
        task.setQuantityPicked(qtyPicked);
        task.setPickingStatus(PickingStatus.COMPLETED);
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            task.setNotes(request.getNotes());
        }
        PickingTask saved = pickingRepository.save(task);
        log.info("Pick task {} COMPLETED — qty={}, bin={}",
                saved.getPickNumber(), qtyPicked, task.getSourceBinCode());

        // --- Orchestrate: check if all picks for the order are done ---
        checkAndAdvanceOrderAfterPick(task.getOutboundOrderId());

        return enrich(saved);
    }

    // -------------------------------------------------------
    // POST-PICK ORCHESTRATION
    // -------------------------------------------------------

    /**
     * After a pick task completes, acquires a pessimistic lock on the outbound order
     * and evaluates whether all active pick tasks are done. If so, transitions the
     * order to PICKED, then creates packing tasks, then transitions to PACKING.
     * The pessimistic lock prevents duplicate packing task creation under concurrency.
     */
    private void checkAndAdvanceOrderAfterPick(Long orderId) {
        // Acquire exclusive row lock on the order before checking/transitioning status
        OutboundOrder order = orderRepository.findByIdAndDeletedFalseWithLock(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Outbound Order not found: " + orderId));

        // Guard: if already transitioned by a concurrent transaction, skip
        OutboundOrderStatus current = order.getOrderStatus();
        if (current == OutboundOrderStatus.PACKING
                || current == OutboundOrderStatus.READY_TO_SHIP
                || current == OutboundOrderStatus.SHIPPED
                || current == OutboundOrderStatus.COMPLETED) {
            log.debug("Order {} already in status {}, skipping post-pick check", orderId, current);
            return;
        }

        List<PickingTask> allTasks =
                pickingRepository.findAllByOutboundOrderIdAndDeletedFalse(orderId);

        // Consider only non-cancelled tasks for completion check
        List<PickingTask> activeTasks = allTasks.stream()
                .filter(t -> t.getPickingStatus() != PickingStatus.CANCELLED)
                .toList();

        if (activeTasks.isEmpty()) {
            log.warn("No active pick tasks found for order {}", orderId);
            return;
        }

        boolean allComplete = activeTasks.stream()
                .allMatch(t -> t.getPickingStatus() == PickingStatus.COMPLETED
                        && t.getQuantityPicked().compareTo(t.getQuantityToPick()) >= 0);

        if (!allComplete) {
            // Some tasks still pending/in-progress
            order.setOrderStatus(OutboundOrderStatus.PICKING);
            orderRepository.save(order);
            log.debug("Not all picks done for order {}. Remaining picking tasks exist.", orderId);
            return;
        }

        // All picking complete → create packing tasks and advance to PACKING
        log.info("All pick tasks COMPLETED for order={}. Transitioning to PACKING.", orderId);

        BigDecimal totalPicked = activeTasks.stream()
                .map(PickingTask::getQuantityPicked)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String quantityUom = activeTasks.get(0).getQuantityUom();

        // Set PICKED first so PackingService sees correct status
        order.setOrderStatus(OutboundOrderStatus.PICKED);
        orderRepository.save(order);

        // Delegate packing task creation to PackingService
        packingService.createPackingTasksForOrder(order, totalPicked, quantityUom);

        // Advance to PACKING
        order.setOrderStatus(OutboundOrderStatus.PACKING);
        orderRepository.save(order);

        log.info("Order {} → PACKING. Packing tasks created.", order.getOrderNumber());
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    private String generatePickNumber() {
        return "PICK-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private PickingTask findTaskOrThrow(Long id) {
        return pickingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pick task not found with id: " + id));
    }

    private OutboundOrder findOrderOrThrow(Long orderId) {
        return orderRepository.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Outbound Order not found with id: " + orderId));
    }

    private void validateStaffActive(Staff staff) {
        if ("INACTIVE".equalsIgnoreCase(staff.getStatus())
                || "TERMINATED".equalsIgnoreCase(staff.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Staff member '" + staff.getDisplayName() + "' is not active.");
        }
    }

    /**
     * Retrieves the inventory record referenced by the pick task using the existing
     * pessimistic-lock query on the inventory repository.
     */
    private Inventory locateInventoryForPick(PickingTask task) {
        // Primary lookup by the specific inventory ID stored at allocation time
        Inventory inv = inventoryRepository.findById(task.getInventoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Inventory record not found for pick task: " + task.getPickNumber()));

        if (inv.getOnHandQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Insufficient inventory for pick task " + task.getPickNumber());
        }
        return inv;
    }

    private PickingTaskResponseDto enrich(PickingTask task) {
        PickingTaskResponseDto dto = mapper.toDto(task);

        // Compute derived field: quantity remaining
        if (task.getQuantityToPick() != null && task.getQuantityPicked() != null) {
            dto.setQuantityRemaining(task.getQuantityToPick().subtract(task.getQuantityPicked())
                    .max(BigDecimal.ZERO));
        }

        // Enrich order context
        orderRepository.findByIdAndDeletedFalse(task.getOutboundOrderId()).ifPresent(o -> {
            dto.setOrderNumber(o.getOrderNumber());
            dto.setCustomerCode(o.getCustomerCode());
            dto.setCustomerName(o.getCustomerName());
        });

        // Enrich line number
        orderLineRepository.findById(task.getOutboundOrderLineId()).ifPresent(l ->
                dto.setLineNumber(l.getLineNumber()));

        return dto;
    }

    private Specification<PickingTask> buildSpec(String search,
                                                  PickingStatus status,
                                                  Long outboundOrderId,
                                                  String staffId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (status != null) {
                predicates.add(cb.equal(root.get("pickingStatus"), status));
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
                        cb.like(cb.lower(root.get("pickNumber")), pattern),
                        cb.like(cb.lower(root.get("skuCode")), pattern),
                        cb.like(cb.lower(root.get("skuName")), pattern),
                        cb.like(cb.lower(root.get("assignedStaffName")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
