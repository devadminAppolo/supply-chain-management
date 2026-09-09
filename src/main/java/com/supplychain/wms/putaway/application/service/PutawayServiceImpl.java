package com.supplychain.wms.putaway.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.BinType;
import com.supplychain.common.domain.enums.InventoryStatus;
import com.supplychain.common.domain.enums.TransactionType;
import com.supplychain.common.domain.enums.ZoneType;
import com.supplychain.wms.inventory.domain.model.Inventory;
import com.supplychain.wms.inventory.infrastructure.repository.InventoryRepository;
import com.supplychain.wms.inventorytransaction.domain.model.InventoryTransaction;
import com.supplychain.wms.inventorytransaction.infrastructure.repository.InventoryTransactionRepository;
import com.supplychain.wms.location.domain.model.Bin;
import com.supplychain.wms.location.domain.model.Zone;
import com.supplychain.wms.location.infrastructure.repository.BinRepository;
import com.supplychain.wms.location.infrastructure.repository.ZoneRepository;
import com.supplychain.wms.putaway.application.dto.AssignStaffRequestDto;
import com.supplychain.wms.putaway.application.dto.CompletePutawayRequestDto;
import com.supplychain.wms.putaway.application.dto.PutawayTaskDto;
import com.supplychain.wms.putaway.domain.enums.PutawayStatus;
import com.supplychain.wms.putaway.domain.model.PutawayTask;
import com.supplychain.wms.putaway.infrastructure.mapper.PutawayTaskMapper;
import com.supplychain.wms.putaway.infrastructure.repository.PutawayTaskRepository;
import com.supplychain.wms.receiving.domain.model.ReceivingPallet;
import com.supplychain.wms.receiving.infrastructure.repository.ReceivingPalletRepository;
import com.supplychain.wms.staff.domain.model.Staff;
import com.supplychain.wms.staff.infrastructure.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PutawayServiceImpl implements PutawayService {

    private final PutawayTaskRepository putawayTaskRepository;
    private final ReceivingPalletRepository receivingPalletRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final BinRepository binRepository;
    private final ZoneRepository zoneRepository;
    private final StaffRepository staffRepository;
    private final PutawayTaskMapper putawayTaskMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PutawayTaskDto> findAll(String search, PutawayStatus status, Long warehouseId, String staffId, Pageable pageable) {
        Specification<PutawayTask> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (status != null) {
                predicates.add(cb.equal(root.get("putawayStatus"), status));
            }
            if (warehouseId != null) {
                predicates.add(cb.equal(root.get("warehouse").get("id"), warehouseId));
            }
            if (staffId != null && !staffId.isBlank()) {
                predicates.add(cb.equal(root.get("assignedStaffId"), staffId.trim()));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("taskNumber")), pattern),
                        cb.like(cb.lower(root.get("itemCode")), pattern),
                        cb.like(cb.lower(root.get("itemName")), pattern),
                        cb.like(cb.lower(root.get("assignedStaffName")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<PutawayTask> page = putawayTaskRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::enrichPutawayTask));
    }

    @Override
    @Transactional(readOnly = true)
    public PutawayTaskDto findById(Long id) {
        PutawayTask task = putawayTaskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Putaway task not found with ID: " + id));
        return enrichPutawayTask(task);
    }

    @Override
    @Transactional
    public PutawayTaskDto assignStaff(Long id, AssignStaffRequestDto request) {
        log.info("Assigning staff '{}' to Putaway Task ID: {}", request.getAssignedStaffId(), id);

        PutawayTask task = putawayTaskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Putaway task not found with ID: " + id));

        if (task.getPutawayStatus() == PutawayStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot reassign staff to a COMPLETED putaway task");
        }
        if (task.getPutawayStatus() == PutawayStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign staff to a CANCELLED putaway task");
        }

        String staffInput = request.getAssignedStaffId() != null ? request.getAssignedStaffId().trim() : "";
        if (staffInput.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff ID or Name is required for assignment");
        }

        // 1. Resolve Staff from Staff Master
        Staff staff = resolveActiveStaff(staffInput, request.getAssignedStaffName());

        // 2. Validate Org / DC Scoping if applicable
        if (task.getOrgId() != null && staff.getOrgId() != null && !task.getOrgId().equals(staff.getOrgId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Staff '%s' belongs to Organization %s, which does not match task Organization %s",
                            staff.getDisplayName(), staff.getOrgId(), task.getOrgId()));
        }
        if (task.getDcId() != null && staff.getDcId() != null && !task.getDcId().equals(staff.getDcId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Staff '%s' belongs to Distribution Center %s, which does not match task DC %s",
                            staff.getDisplayName(), staff.getDcId(), task.getDcId()));
        }

        // 3. Assign Staff & Update Status
        task.setAssignedStaffId(String.valueOf(staff.getId()));
        task.setAssignedStaffName(staff.getDisplayName());
        task.setPutawayStatus(PutawayStatus.ASSIGNED);
        task.setStatus("ASSIGNED");

        PutawayTask saved = putawayTaskRepository.save(task);
        log.info("Staff '{}' (ID: {}) successfully assigned to Putaway Task {}", staff.getDisplayName(), staff.getId(), saved.getTaskNumber());
        return enrichPutawayTask(saved);
    }

    @Override
    @Transactional
    public PutawayTaskDto startPutaway(Long id) {
        log.info("Starting Putaway Task ID: {}", id);

        PutawayTask task = putawayTaskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Putaway task not found with ID: " + id));

        if (task.getPutawayStatus() == PutawayStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Putaway task has already been completed");
        }
        if (task.getPutawayStatus() == PutawayStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot start a CANCELLED putaway task");
        }

        task.setPutawayStatus(PutawayStatus.IN_PROGRESS);
        task.setStatus("IN_PROGRESS");
        if (task.getStartedAt() == null) {
            task.setStartedAt(LocalDateTime.now());
        }

        PutawayTask saved = putawayTaskRepository.save(task);
        return enrichPutawayTask(saved);
    }

    @Override
    @Transactional
    public PutawayTaskDto completePutaway(Long id, CompletePutawayRequestDto request) {
        log.info("Completing Putaway Task ID: {} to destination bin ID: {}", id, request.getPutawayLocationId());

        PutawayTask task = putawayTaskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Putaway task not found with ID: " + id));

        if (task.getPutawayStatus() == PutawayStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Putaway task has already been completed");
        }
        if (task.getPutawayStatus() == PutawayStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot complete a CANCELLED putaway task");
        }

        ReceivingPallet pallet = task.getPallet();
        Long warehouseId = task.getWarehouse() != null ? task.getWarehouse().getId() : 1L;
        Long destinationBinId = request.getPutawayLocationId();

        // 1. Validate Destination Location
        Bin destBin = binRepository.findById(destinationBinId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination putaway location (bin) not found with ID: " + destinationBinId));

        if (destBin.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destination putaway location (bin) is inactive/deleted: " + destBin.getBinCode());
        }

        if (task.getWarehouse() != null && destBin.getWarehouseId() != null && !destBin.getWarehouseId().equals(task.getWarehouse().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Destination location %s belongs to warehouse %s, which does not match task warehouse %s",
                            destBin.getBinCode(), destBin.getWarehouseId(), task.getWarehouse().getId()));
        }

        // Validate destination is NOT staging
        Zone destZone = null;
        if (destBin.getZoneId() != null) {
            destZone = zoneRepository.findById(destBin.getZoneId()).orElse(null);
        }

        if (destBin.getBinType() == BinType.STAGING || (destZone != null && destZone.getZoneType() == ZoneType.RECEIVING)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Putaway destination location cannot be a STAGING location. Destination must be ACTIVE or RESERVED.");
        }

        // 2. Validate & Move Inventory from STAGING to Destination
        Long stagingBinId = task.getStagingBinId();
        Long skuId = task.getSkuId();
        Long productId = task.getProductId() != null ? task.getProductId() : 1L;
        Long uomId = task.getUomId() != null ? task.getUomId() : 1L;
        BigDecimal moveQty = task.getQuantity();
        String batchNumber = pallet != null ? pallet.getBatchNumber() : null;
        String serialNumber = pallet != null ? pallet.getSerialNumber() : null;

        // Deduct from STAGING
        Inventory stagingInv = inventoryRepository.findInventoryByKey(
                warehouseId, stagingBinId, skuId, batchNumber, serialNumber, InventoryStatus.AVAILABLE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("No inventory found in staging location (binId=%s) for SKU %s", stagingBinId, task.getItemCode())));

        if (stagingInv.getAvailableQuantity() == null || stagingInv.getAvailableQuantity().compareTo(moveQty) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Insufficient inventory available in staging location. Available: %s, Required: %s",
                            stagingInv.getAvailableQuantity(), moveQty));
        }

        BigDecimal stagingQtyBefore = stagingInv.getOnHandQuantity();
        BigDecimal newStagingOnHand = stagingQtyBefore.subtract(moveQty);
        stagingInv.setOnHandQuantity(newStagingOnHand);
        BigDecimal stagingReserved = stagingInv.getReservedQuantity() != null ? stagingInv.getReservedQuantity() : BigDecimal.ZERO;
        BigDecimal stagingAllocated = stagingInv.getAllocatedQuantity() != null ? stagingInv.getAllocatedQuantity() : BigDecimal.ZERO;
        stagingInv.setAvailableQuantity(newStagingOnHand.subtract(stagingReserved).subtract(stagingAllocated));
        inventoryRepository.save(stagingInv);

        // Credit to Destination Location
        Optional<Inventory> destInvOpt = inventoryRepository.findInventoryByKey(
                warehouseId, destinationBinId, skuId, batchNumber, serialNumber, InventoryStatus.AVAILABLE);

        Inventory targetDestInv;
        if (destInvOpt.isPresent()) {
            targetDestInv = destInvOpt.get();
            BigDecimal destOnHandBefore = targetDestInv.getOnHandQuantity() != null ? targetDestInv.getOnHandQuantity() : BigDecimal.ZERO;
            BigDecimal newDestOnHand = destOnHandBefore.add(moveQty);
            targetDestInv.setOnHandQuantity(newDestOnHand);
            BigDecimal destReserved = targetDestInv.getReservedQuantity() != null ? targetDestInv.getReservedQuantity() : BigDecimal.ZERO;
            BigDecimal destAllocated = targetDestInv.getAllocatedQuantity() != null ? targetDestInv.getAllocatedQuantity() : BigDecimal.ZERO;
            targetDestInv.setAvailableQuantity(newDestOnHand.subtract(destReserved).subtract(destAllocated));
        } else {
            targetDestInv = Inventory.builder()
                    .orgId(task.getOrgId())
                    .dcId(task.getDcId())
                    .warehouseId(warehouseId)
                    .binId(destinationBinId)
                    .productId(productId)
                    .skuId(skuId)
                    .uomId(uomId)
                    .lotNumber(batchNumber)
                    .serialNumber(serialNumber)
                    .inventoryStatus(InventoryStatus.AVAILABLE)
                    .onHandQuantity(moveQty)
                    .availableQuantity(moveQty)
                    .reservedQuantity(BigDecimal.ZERO)
                    .allocatedQuantity(BigDecimal.ZERO)
                    .status("ACTIVE")
                    .deleted(false)
                    .build();
        }
        Inventory savedDestInv = inventoryRepository.save(targetDestInv);

        // 3. Record PUTAWAY InventoryTransaction
        InventoryTransaction transaction = InventoryTransaction.builder()
                .orgId(task.getOrgId())
                .dcId(task.getDcId())
                .warehouseId(warehouseId)
                .inventoryId(savedDestInv.getId())
                .productId(productId)
                .skuId(skuId)
                .uomId(uomId)
                .fromBinId(stagingBinId)
                .toBinId(destinationBinId)
                .transactionType(TransactionType.PUTAWAY)
                .quantity(moveQty)
                .quantityBefore(stagingQtyBefore)
                .quantityAfter(stagingInv.getOnHandQuantity())
                .lotNumber(batchNumber)
                .serialNumber(serialNumber)
                .referenceType("PUTAWAY_TASK")
                .referenceId(task.getTaskNumber())
                .reasonCode("PUTAWAY_COMPLETED")
                .transactionDate(LocalDateTime.now())
                .deleted(false)
                .build();
        inventoryTransactionRepository.save(transaction);

        // 4. Update Pallet current location
        if (pallet != null) {
            pallet.setCurrentBinId(destinationBinId);
            pallet.setPalletStatus("STORED");
            pallet.setStatus("STORED");
            receivingPalletRepository.save(pallet);
        }

        // 5. Finalize Task
        task.setPutawayBinId(destinationBinId);
        task.setPutawayStatus(PutawayStatus.COMPLETED);
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            task.setNotes(task.getNotes() != null ? task.getNotes() + " | " + request.getNotes() : request.getNotes());
        }

        PutawayTask savedTask = putawayTaskRepository.save(task);
        log.info("Putaway Task {} completed successfully to bin {}", savedTask.getTaskNumber(), destBin.getBinCode());

        return enrichPutawayTask(savedTask);
    }

    private PutawayTaskDto enrichPutawayTask(PutawayTask task) {
        PutawayTaskDto dto = putawayTaskMapper.toDto(task);
        if (dto != null) {
            if (dto.getStagingLocationId() != null) {
                binRepository.findById(dto.getStagingLocationId())
                        .ifPresent(b -> dto.setStagingBinCode(b.getBinCode()));
            }
            if (dto.getPutawayLocationId() != null) {
                binRepository.findById(dto.getPutawayLocationId())
                        .ifPresent(b -> dto.setPutawayBinCode(b.getBinCode()));
            }
        }
        return dto;
    }

    private Staff resolveActiveStaff(String staffInput, String customStaffName) {
        Staff staff = null;

        // Try numeric ID lookup
        try {
            Long staffId = Long.parseLong(staffInput);
            staff = staffRepository.findByIdAndDeletedFalse(staffId).orElse(null);
        } catch (NumberFormatException ignored) {
            // Not a numeric ID
        }

        // Try username lookup
        if (staff == null) {
            staff = staffRepository.findByUserNameAndDeletedFalse(staffInput).orElse(null);
        }

        // Try display name lookup (firstName + lastName or firstName)
        if (staff == null) {
            List<Staff> allStaff = staffRepository.findAllByDeletedFalseOrderByFirstNameAsc();
            for (Staff s : allStaff) {
                if (s.getDisplayName().equalsIgnoreCase(staffInput)
                        || s.getFirstName().equalsIgnoreCase(staffInput)
                        || (customStaffName != null && s.getDisplayName().equalsIgnoreCase(customStaffName.trim()))) {
                    staff = s;
                    break;
                }
            }
        }

        if (staff == null) {
            log.warn("Staff member not found with identifier: {}", staffInput);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff member not found: " + staffInput);
        }

        if (!"ACTIVE".equalsIgnoreCase(staff.getStatus())) {
            log.warn("Staff member {} is inactive with status: {}", staff.getDisplayName(), staff.getStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff member is inactive and cannot be assigned: " + staff.getDisplayName());
        }

        return staff;
    }
}
