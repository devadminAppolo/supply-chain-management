package com.supplychain.wms.receiving.application.service;

import com.supplychain.catalog.domain.model.Product;
import com.supplychain.catalog.domain.model.Sku;
import com.supplychain.catalog.infrastructure.repository.ProductRepository;
import com.supplychain.catalog.infrastructure.repository.SkuRepository;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.BinType;
import com.supplychain.common.domain.enums.InventoryStatus;
import com.supplychain.common.domain.enums.TransactionType;
import com.supplychain.common.domain.enums.ZoneType;
import com.supplychain.procurement.asn.domain.enums.AsnLineStatus;
import com.supplychain.procurement.asn.domain.enums.AsnStatus;
import com.supplychain.procurement.asn.domain.model.AdvanceShippingNotice;
import com.supplychain.procurement.asn.domain.model.AdvanceShippingNoticeLine;
import com.supplychain.procurement.asn.infrastructure.repository.AdvanceShippingNoticeLineRepository;
import com.supplychain.procurement.asn.infrastructure.repository.AdvanceShippingNoticeRepository;
import com.supplychain.procurement.purchaseorder.domain.enums.LineStatus;
import com.supplychain.procurement.purchaseorder.domain.enums.PoStatus;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrder;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrderLineItem;
import com.supplychain.procurement.purchaseorder.infrastructure.repository.PurchaseOrderLineItemRepository;
import com.supplychain.procurement.purchaseorder.infrastructure.repository.PurchaseOrderRepository;
import com.supplychain.wms.inventory.domain.model.Inventory;
import com.supplychain.wms.inventory.infrastructure.repository.InventoryRepository;
import com.supplychain.wms.inventorytransaction.domain.model.InventoryTransaction;
import com.supplychain.wms.inventorytransaction.infrastructure.repository.InventoryTransactionRepository;
import com.supplychain.wms.location.domain.model.Bin;
import com.supplychain.wms.location.domain.model.Warehouse;
import com.supplychain.wms.location.domain.model.Zone;
import com.supplychain.wms.location.infrastructure.repository.BinRepository;
import com.supplychain.wms.location.infrastructure.repository.WarehouseRepository;
import com.supplychain.wms.location.infrastructure.repository.ZoneRepository;
import com.supplychain.wms.putaway.domain.enums.PutawayStatus;
import com.supplychain.wms.putaway.domain.model.PutawayTask;
import com.supplychain.wms.putaway.infrastructure.repository.PutawayTaskRepository;
import com.supplychain.wms.pallet.application.service.PalletService;
import com.supplychain.wms.pallet.domain.model.Pallet;
import com.supplychain.wms.receiving.application.dto.AsnReceivingDetailsDto;
import com.supplychain.wms.receiving.application.dto.AsnReceivingLineDto;
import com.supplychain.wms.receiving.application.dto.ReceivingCreateRequestDto;
import com.supplychain.wms.receiving.application.dto.ReceivingPalletRequestDto;
import com.supplychain.wms.receiving.application.dto.ReceivingPalletResponseDto;
import com.supplychain.wms.receiving.application.dto.ReceivingResponseDto;
import com.supplychain.wms.receiving.domain.enums.ReceivingStatus;
import com.supplychain.wms.receiving.domain.model.Receiving;
import com.supplychain.wms.receiving.domain.model.ReceivingPallet;
import com.supplychain.wms.receiving.infrastructure.mapper.ReceivingMapper;
import com.supplychain.wms.receiving.infrastructure.mapper.ReceivingPalletMapper;
import com.supplychain.wms.receiving.infrastructure.repository.ReceivingPalletRepository;
import com.supplychain.wms.receiving.infrastructure.repository.ReceivingRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceivingServiceImpl implements ReceivingService {

    private final ReceivingRepository receivingRepository;
    private final ReceivingPalletRepository receivingPalletRepository;
    private final AdvanceShippingNoticeRepository asnRepository;
    private final AdvanceShippingNoticeLineRepository asnLineRepository;
    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderLineItemRepository poLineRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final PutawayTaskRepository putawayTaskRepository;
    private final BinRepository binRepository;
    private final ZoneRepository zoneRepository;
    private final WarehouseRepository warehouseRepository;
    private final SkuRepository skuRepository;
    private final ProductRepository productRepository;
    private final PalletService palletService;
    private final ReceivingMapper receivingMapper;
    private final ReceivingPalletMapper receivingPalletMapper;

    @Override
    @Transactional(readOnly = true)
    public AsnReceivingDetailsDto getAsnReceivingDetails(Long asnId) {
        log.info("Fetching ASN receiving details for ASN ID: {}", asnId);
        AdvanceShippingNotice asn = asnRepository.findByIdAndDeletedFalse(asnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advance Shipping Notice not found with ID: " + asnId));

        PurchaseOrder po = asn.getPurchaseOrder();

        List<AsnReceivingLineDto> lines = new ArrayList<>();
        if (asn.getLines() != null) {
            for (AdvanceShippingNoticeLine line : asn.getLines()) {
                if (line.isDeleted()) {
                    continue;
                }
                BigDecimal prevReceived = receivingPalletRepository.sumReceivedQuantityByAsnLineId(line.getId());
                if (prevReceived == null) {
                    prevReceived = BigDecimal.ZERO;
                }

                BigDecimal remaining = line.getShipmentQuantity().subtract(prevReceived);
                if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                    remaining = BigDecimal.ZERO;
                }

                PurchaseOrderLineItem poLine = line.getPurchaseOrderLine();
                Long skuId = null;
                Long productId = null;
                Long uomId = null;
                String itemCodeToLookup = line.getItemCode() != null ? line.getItemCode() : (poLine != null ? poLine.getItemCode() : null);
                if (itemCodeToLookup != null) {
                    Optional<Sku> skuOpt = skuRepository.findBySkuCodeAndDeletedFalse(itemCodeToLookup);
                    if (skuOpt.isPresent()) {
                        skuId = skuOpt.get().getId();
                        productId = skuOpt.get().getProductId();
                        Optional<Product> prodOpt = productRepository.findById(productId);
                        if (prodOpt.isPresent()) {
                            uomId = prodOpt.get().getUomId();
                        }
                    }
                }

                lines.add(AsnReceivingLineDto.builder()
                        .asnLineId(line.getId())
                        .poLineId(poLine != null ? poLine.getId() : null)
                        .lineNumber(line.getLineNumber())
                        .productId(productId)
                        .skuId(skuId)
                        .uomId(uomId)
                        .itemCode(line.getItemCode())
                        .itemName(line.getItemName())
                        .supplierItemCode(line.getSupplierItemCode())
                        .itemGtin(line.getItemGtin())
                        .batchNumber(line.getBatchNumber())
                        .serialNumber(line.getSerialNumber())
                        .manufacturingDate(line.getManufacturingDate())
                        .expiryDate(line.getExpiryDate())
                        .quantityOrdered(line.getQuantityOrdered())
                        .asnQuantity(line.getShipmentQuantity())
                        .previouslyReceivedQuantity(prevReceived)
                        .remainingQuantity(remaining)
                        .uom(line.getQuantityUom())
                        .unitPrice(line.getUnitPrice())
                        .currencyCode(line.getCurrencyCode())
                        .lineStatus(line.getLineStatus() != null ? line.getLineStatus().name() : null)
                        .build());
            }
        }

        return AsnReceivingDetailsDto.builder()
                .asnId(asn.getId())
                .asnNumber(asn.getAsnNumber())
                .asnStatus(asn.getAsnStatus() != null ? asn.getAsnStatus().name() : null)
                .purchaseOrderId(po != null ? po.getId() : null)
                .purchaseOrderNumber(po != null ? po.getPoNumber() : null)
                .supplierId(asn.getSupplierId())
                .supplierName(asn.getSupplierName())
                .warehouseId(asn.getWarehouseId())
                .warehouseName(asn.getWarehouseName())
                .shipmentDate(asn.getShipmentDate())
                .expectedArrivalDate(asn.getExpectedArrivalDate())
                .carrierId(asn.getCarrierId())
                .carrierName(asn.getCarrierName())
                .trackingNumber(asn.getTrackingNumber())
                .containerNumber(asn.getContainerNumber())
                .vehicleNumber(asn.getVehicleNumber())
                .lines(lines)
                .build();
    }

    @Override
    @Transactional
    public ReceivingResponseDto createReceiving(ReceivingCreateRequestDto request) {
        log.info("Creating Inbound Receiving for ASN ID: {}", request.getAsnId());

        AdvanceShippingNotice asn = asnRepository.findByIdAndDeletedFalse(request.getAsnId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advance Shipping Notice not found with ID: " + request.getAsnId()));

        if (asn.getAsnStatus() == AsnStatus.CANCELLED || asn.getAsnStatus() == AsnStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ASN is not eligible for receiving in " + asn.getAsnStatus() + " status");
        }

        if (asn.getAsnStatus() == AsnStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ASN is in DRAFT status and must be submitted/in-transit before receiving");
        }

        PurchaseOrder po = asn.getPurchaseOrder();
        if (po == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Referenced ASN is not associated with a valid Purchase Order");
        }

        String receivingNumber = request.getReceivingNumber();
        if (receivingNumber != null && !receivingNumber.isBlank()) {
            receivingNumber = receivingNumber.trim();
            if (receivingRepository.existsByReceivingNumberAndDeletedFalse(receivingNumber)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Receiving number already exists: " + receivingNumber);
            }
        } else {
            receivingNumber = generateReceivingNumber();
        }

        List<ReceivingPalletRequestDto> palletRequests = request.getPallets();
        if (palletRequests == null || palletRequests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiving must contain at least one pallet");
        }

        Map<Long, AdvanceShippingNoticeLine> asnLineMap = asn.getLines().stream()
                .filter(l -> !l.isDeleted())
                .collect(Collectors.toMap(AdvanceShippingNoticeLine::getId, Function.identity()));

        // Group quantities by ASN line to validate remaining quantities
        Map<Long, BigDecimal> requestedQtyPerAsnLine = new HashMap<>();
        for (ReceivingPalletRequestDto palletReq : palletRequests) {
            if (palletReq.getAsnLineId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ASN line ID is required for each pallet");
            }
            if (!asnLineMap.containsKey(palletReq.getAsnLineId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ASN line ID " + palletReq.getAsnLineId() + " does not belong to ASN " + asn.getAsnNumber());
            }
            if (palletReq.getQuantity() == null || palletReq.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pallet quantity must be greater than 0");
            }
            requestedQtyPerAsnLine.merge(palletReq.getAsnLineId(), palletReq.getQuantity(), BigDecimal::add);
        }

        // Validate over-receiving per line
        for (Map.Entry<Long, BigDecimal> entry : requestedQtyPerAsnLine.entrySet()) {
            Long lineId = entry.getKey();
            BigDecimal totalRequested = entry.getValue();
            AdvanceShippingNoticeLine asnLine = asnLineMap.get(lineId);

            BigDecimal prevReceived = receivingPalletRepository.sumReceivedQuantityByAsnLineId(lineId);
            if (prevReceived == null) {
                prevReceived = BigDecimal.ZERO;
            }

            BigDecimal remaining = asnLine.getShipmentQuantity().subtract(prevReceived);
            if (totalRequested.compareTo(remaining) > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        String.format("Receiving quantity %s exceeds remaining ASN quantity %s for line item %s (Ordered: %s, Shipped: %s, Prev Received: %s)",
                                totalRequested, remaining, asnLine.getItemCode(), asnLine.getQuantityOrdered(), asnLine.getShipmentQuantity(), prevReceived));
            }
        }

        // Resolve warehouse
        Warehouse warehouse = resolveWarehouse(asn, po);

        Receiving receiving = Receiving.builder()
                .orgId(asn.getOrgId())
                .dcId(asn.getDcId())
                .receivingNumber(receivingNumber)
                .advanceShippingNotice(asn)
                .asnNumber(asn.getAsnNumber())
                .purchaseOrder(po)
                .purchaseOrderNumber(po.getPoNumber())
                .supplierId(asn.getSupplierId())
                .supplierName(asn.getSupplierName())
                .warehouse(warehouse)
                .warehouseCode(warehouse != null ? warehouse.getWarehouseCode() : null)
                .warehouseName(warehouse != null ? warehouse.getWarehouseName() : null)
                .arrivalTime(request.getArrivalTime() != null ? request.getArrivalTime() : LocalDateTime.now())
                .receivingDate(request.getReceivingDate() != null ? request.getReceivingDate() : LocalDateTime.now())
                .receivingStatus(ReceivingStatus.IN_PROGRESS)
                .status("IN_PROGRESS")
                .notes(request.getNotes())
                .deleted(false)
                .pallets(new ArrayList<>())
                .build();

        int palletIndex = 1;
        for (ReceivingPalletRequestDto palletReq : palletRequests) {
            AdvanceShippingNoticeLine asnLine = asnLineMap.get(palletReq.getAsnLineId());
            PurchaseOrderLineItem poLine = asnLine.getPurchaseOrderLine();

            // Validate staging bin
            Bin stagingBin = validateStagingBin(palletReq.getStagingLocationId(), warehouse);

            // Resolve item details
            Long skuId = null;
            Long productId = null;
            Long uomId = null;
            String itemCodeToLookup = asnLine.getItemCode() != null ? asnLine.getItemCode() : (poLine != null ? poLine.getItemCode() : null);
            if (itemCodeToLookup != null) {
                Optional<Sku> skuOpt = skuRepository.findBySkuCodeAndDeletedFalse(itemCodeToLookup);
                if (skuOpt.isPresent()) {
                    skuId = skuOpt.get().getId();
                    productId = skuOpt.get().getProductId();
                    Optional<Product> prodOpt = productRepository.findById(productId);
                    if (prodOpt.isPresent()) {
                        uomId = prodOpt.get().getUomId();
                    }
                }
            }
            if (skuId == null) {
                skuId = 1L;
                productId = 1L;
                uomId = 1L;
            }

            // Resolve or create Pallet master record
            Pallet palletMaster = palletService.resolveOrCreatePallet(
                    palletReq.getPalletMasterId(),
                    palletReq.getPalletNumber(),
                    palletReq.getLpnNumber(),
                    asn.getOrgId(),
                    asn.getDcId()
            );

            String palletNum = palletMaster != null ? palletMaster.getPalletNumber() : palletReq.getPalletNumber();
            if (palletNum == null || palletNum.isBlank()) {
                palletNum = String.format("PLT-%s-%02d", receivingNumber, palletIndex++);
            }

            String lpnNum = palletMaster != null ? palletMaster.getLpnNumber() : palletReq.getLpnNumber();
            if (lpnNum == null || lpnNum.isBlank()) {
                lpnNum = "LPN-" + palletNum;
            }

            ReceivingPallet pallet = ReceivingPallet.builder()
                    .orgId(asn.getOrgId())
                    .dcId(asn.getDcId())
                    .receiving(receiving)
                    .advanceShippingNotice(asn)
                    .advanceShippingNoticeLine(asnLine)
                    .purchaseOrder(po)
                    .purchaseOrderLine(poLine)
                    .palletMasterId(palletMaster != null ? palletMaster.getId() : null)
                    .palletNumber(palletNum)
                    .lpnNumber(lpnNum)
                    .productId(productId)
                    .skuId(skuId)
                    .uomId(uomId)
                    .itemCode(asnLine.getItemCode())
                    .itemName(asnLine.getItemName())
                    .quantity(palletReq.getQuantity())
                    .condition(palletReq.getCondition() != null ? palletReq.getCondition() : "GOOD")
                    .stagingBinId(stagingBin.getId())
                    .currentBinId(stagingBin.getId())
                    .batchNumber(palletReq.getBatchNumber() != null ? palletReq.getBatchNumber() : asnLine.getBatchNumber())
                    .serialNumber(palletReq.getSerialNumber() != null ? palletReq.getSerialNumber() : asnLine.getSerialNumber())
                    .manufacturingDate(palletReq.getManufacturingDate() != null ? palletReq.getManufacturingDate() : asnLine.getManufacturingDate())
                    .expiryDate(palletReq.getExpiryDate() != null ? palletReq.getExpiryDate() : asnLine.getExpiryDate())
                    .receivedAt(LocalDateTime.now())
                    .palletStatus("RECEIVED_STAGING")
                    .status("RECEIVED_STAGING")
                    .deleted(false)
                    .build();

            receiving.addPallet(pallet);
        }

        Receiving saved = receivingRepository.save(receiving);
        log.info("Receiving {} created successfully with ID: {} and {} pallets", saved.getReceivingNumber(), saved.getId(), saved.getPallets().size());
        return enrichReceivingResponse(saved);
    }

    @Override
    @Transactional
    public ReceivingResponseDto completeReceiving(Long receivingId) {
        log.info("Completing Inbound Receiving ID: {}", receivingId);

        Receiving receiving = receivingRepository.findByIdAndDeletedFalse(receivingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiving record not found with ID: " + receivingId));

        if (receiving.getReceivingStatus() == ReceivingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Receiving has already been completed");
        }

        if (receiving.getReceivingStatus() == ReceivingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot complete a CANCELLED receiving");
        }

        AdvanceShippingNotice asn = receiving.getAdvanceShippingNotice();
        PurchaseOrder po = receiving.getPurchaseOrder();
        Warehouse warehouse = receiving.getWarehouse();
        Long warehouseId = warehouse != null ? warehouse.getId() : 1L;

        // Process pallets: Create/Update STAGING inventory, record RECEIPT transactions, spawn PutawayTasks
        for (ReceivingPallet pallet : receiving.getPallets()) {
            if (pallet.isDeleted()) {
                continue;
            }

            Long stagingBinId = pallet.getStagingBinId();
            Long skuId = pallet.getSkuId();
            Long productId = pallet.getProductId();
            Long uomId = pallet.getUomId();
            BigDecimal qty = pallet.getQuantity();
            String batchNum = pallet.getBatchNumber();
            String serialNum = pallet.getSerialNumber();

            // 1. STAGING Inventory update
            Optional<Inventory> existingInvOpt = inventoryRepository.findInventoryByKey(
                    warehouseId, stagingBinId, skuId, batchNum, serialNum, InventoryStatus.AVAILABLE);

            BigDecimal qtyBefore = BigDecimal.ZERO;
            Inventory targetInventory;
            if (existingInvOpt.isPresent()) {
                targetInventory = existingInvOpt.get();
                qtyBefore = targetInventory.getOnHandQuantity() != null ? targetInventory.getOnHandQuantity() : BigDecimal.ZERO;
                BigDecimal newOnHand = qtyBefore.add(qty);
                targetInventory.setOnHandQuantity(newOnHand);
                BigDecimal reserved = targetInventory.getReservedQuantity() != null ? targetInventory.getReservedQuantity() : BigDecimal.ZERO;
                BigDecimal allocated = targetInventory.getAllocatedQuantity() != null ? targetInventory.getAllocatedQuantity() : BigDecimal.ZERO;
                targetInventory.setAvailableQuantity(newOnHand.subtract(reserved).subtract(allocated));
            } else {
                targetInventory = Inventory.builder()
                        .orgId(receiving.getOrgId())
                        .dcId(receiving.getDcId())
                        .warehouseId(warehouseId)
                        .binId(stagingBinId)
                        .productId(productId != null ? productId : 1L)
                        .skuId(skuId)
                        .uomId(uomId != null ? uomId : 1L)
                        .lotNumber(batchNum)
                        .serialNumber(serialNum)
                        .inventoryStatus(InventoryStatus.AVAILABLE)
                        .onHandQuantity(qty)
                        .availableQuantity(qty)
                        .reservedQuantity(BigDecimal.ZERO)
                        .allocatedQuantity(BigDecimal.ZERO)
                        .status("ACTIVE")
                        .deleted(false)
                        .build();
            }
            Inventory savedInv = inventoryRepository.save(targetInventory);

            // 2. Record RECEIPT InventoryTransaction
            InventoryTransaction transaction = InventoryTransaction.builder()
                    .orgId(receiving.getOrgId())
                    .dcId(receiving.getDcId())
                    .warehouseId(warehouseId)
                    .inventoryId(savedInv.getId())
                    .productId(productId)
                    .skuId(skuId)
                    .uomId(uomId)
                    .fromBinId(null)
                    .toBinId(stagingBinId)
                    .transactionType(TransactionType.RECEIPT)
                    .quantity(qty)
                    .quantityBefore(qtyBefore)
                    .quantityAfter(savedInv.getOnHandQuantity())
                    .lotNumber(batchNum)
                    .serialNumber(serialNum)
                    .referenceType("RECEIVING")
                    .referenceId(receiving.getReceivingNumber())
                    .reasonCode("PO_RECEIPT")
                    .transactionDate(LocalDateTime.now())
                    .deleted(false)
                    .build();
            inventoryTransactionRepository.save(transaction);

            // 3. Create PutawayTask (Step 17, 18, 19, 29)
            PutawayTask putawayTask = PutawayTask.builder()
                    .orgId(receiving.getOrgId())
                    .dcId(receiving.getDcId())
                    .taskNumber(generatePutawayTaskNumber())
                    .receiving(receiving)
                    .pallet(pallet)
                    .advanceShippingNotice(asn)
                    .purchaseOrder(po)
                    .warehouse(warehouse)
                    .productId(productId)
                    .skuId(skuId)
                    .uomId(uomId)
                    .itemCode(pallet.getItemCode())
                    .itemName(pallet.getItemName())
                    .quantity(qty)
                    .stagingBinId(stagingBinId)
                    .putawayBinId(null)
                    .assignedStaffId(null)
                    .assignedStaffName(null)
                    .putawayStatus(PutawayStatus.NOT_ASSIGNED)
                    .status("NOT_ASSIGNED")
                    .startedAt(null)
                    .completedAt(null)
                    .notes("Generated from receiving " + receiving.getReceivingNumber())
                    .deleted(false)
                    .build();
            putawayTaskRepository.save(putawayTask);

            // 4. Mark Pallet Master record as IN_USE
            if (pallet.getPalletMasterId() != null) {
                palletService.markPalletInUse(pallet.getPalletMasterId());
            }
        }

        // 4. Update ASN Line & Header Status
        boolean allAsnLinesReceived = true;
        if (asn.getLines() != null) {
            for (AdvanceShippingNoticeLine asnLine : asn.getLines()) {
                if (asnLine.isDeleted()) {
                    continue;
                }
                BigDecimal totalRecv = receivingPalletRepository.sumReceivedQuantityByAsnLineId(asnLine.getId());
                if (totalRecv == null) {
                    totalRecv = BigDecimal.ZERO;
                }

                if (totalRecv.compareTo(asnLine.getShipmentQuantity()) >= 0) {
                    asnLine.setLineStatus(AsnLineStatus.RECEIVED);
                } else if (totalRecv.compareTo(BigDecimal.ZERO) > 0) {
                    asnLine.setLineStatus(AsnLineStatus.PARTIALLY_RECEIVED);
                    allAsnLinesReceived = false;
                } else {
                    allAsnLinesReceived = false;
                }
                asnLineRepository.save(asnLine);
            }
        }

        if (allAsnLinesReceived) {
            asn.setAsnStatus(AsnStatus.RECEIVED);
        } else {
            asn.setAsnStatus(AsnStatus.PARTIALLY_RECEIVED);
        }
        asnRepository.save(asn);

        // 5. Update PO Line Received Quantities
        if (po.getLineItems() != null) {
            boolean allPoLinesReceived = true;
            for (PurchaseOrderLineItem poLine : po.getLineItems()) {
                if (poLine.isDeleted()) {
                    continue;
                }
                BigDecimal totalPoRecv = receivingPalletRepository.sumReceivedQuantityByPoLineId(poLine.getId());
                if (totalPoRecv == null) {
                    totalPoRecv = BigDecimal.ZERO;
                }
                poLine.setQuantityReceived(totalPoRecv);

                if (totalPoRecv.compareTo(poLine.getQuantityOrdered()) >= 0) {
                    poLine.setLineStatus(LineStatus.CLOSED);
                } else {
                    allPoLinesReceived = false;
                }
                poLineRepository.save(poLine);
            }

            if (allPoLinesReceived) {
                po.setPoStatus(PoStatus.CLOSED);
                poRepository.save(po);
            }
        }

        // 6. Finalize Receiving Status
        receiving.setReceivingStatus(ReceivingStatus.COMPLETED);
        receiving.setStatus("COMPLETED");
        Receiving saved = receivingRepository.save(receiving);
        log.info("Receiving {} completed successfully", saved.getReceivingNumber());

        return enrichReceivingResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceivingResponseDto findById(Long id) {
        Receiving receiving = receivingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiving record not found with ID: " + id));
        return enrichReceivingResponse(receiving);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReceivingResponseDto> findAll(String search, ReceivingStatus status, Long warehouseId, Pageable pageable) {
        Specification<Receiving> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (status != null) {
                predicates.add(cb.equal(root.get("receivingStatus"), status));
            }
            if (warehouseId != null) {
                predicates.add(cb.equal(root.get("warehouse").get("id"), warehouseId));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("receivingNumber")), pattern),
                        cb.like(cb.lower(root.get("asnNumber")), pattern),
                        cb.like(cb.lower(root.get("purchaseOrderNumber")), pattern),
                        cb.like(cb.lower(root.get("supplierName")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Receiving> page = receivingRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::enrichReceivingResponse));
    }

    private Bin validateStagingBin(Long binId, Warehouse warehouse) {
        Bin bin = binRepository.findById(binId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staging location (bin) not found with ID: " + binId));

        if (bin.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staging location (bin) is inactive/deleted: " + bin.getBinCode());
        }

        if (warehouse != null && bin.getWarehouseId() != null && !bin.getWarehouseId().equals(warehouse.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Staging location %s belongs to warehouse %s, which does not match receiving warehouse %s",
                            bin.getBinCode(), bin.getWarehouseId(), warehouse.getId()));
        }

        Zone zone = null;
        if (bin.getZoneId() != null) {
            zone = zoneRepository.findById(bin.getZoneId()).orElse(null);
        }

        boolean isStaging = bin.getBinType() == BinType.STAGING ||
                (zone != null && zone.getZoneType() == ZoneType.RECEIVING);

        if (!isStaging) {
            log.warn("Bin {} is not strictly STAGING (type={}, zone={}), accepting as staging bin",
                    bin.getBinCode(), bin.getBinType(), zone != null ? zone.getZoneType() : "N/A");
        }

        return bin;
    }

    private Warehouse resolveWarehouse(AdvanceShippingNotice asn, PurchaseOrder po) {
        if (asn.getWarehouseId() != null) {
            try {
                Long whId = Long.parseLong(asn.getWarehouseId());
                return warehouseRepository.findByIdAndDeletedFalse(whId).orElse(null);
            } catch (NumberFormatException ignored) {
                return warehouseRepository.findByWarehouseCodeAndDeletedFalse(asn.getWarehouseId()).orElse(null);
            }
        }
        if (po.getWarehouse() != null) {
            try {
                Long whId = Long.parseLong(po.getWarehouse());
                return warehouseRepository.findByIdAndDeletedFalse(whId).orElse(null);
            } catch (NumberFormatException ignored) {
                return warehouseRepository.findByWarehouseCodeAndDeletedFalse(po.getWarehouse()).orElse(null);
            }
        }
        return warehouseRepository.findAllByDeletedFalseOrderByWarehouseNameAsc().stream().findFirst().orElse(null);
    }

    private ReceivingResponseDto enrichReceivingResponse(Receiving receiving) {
        ReceivingResponseDto dto = receivingMapper.toDto(receiving);
        if (dto != null && dto.getPallets() != null) {
            for (ReceivingPalletResponseDto palletDto : dto.getPallets()) {
                if (palletDto.getStagingLocationId() != null) {
                    binRepository.findById(palletDto.getStagingLocationId())
                            .ifPresent(b -> palletDto.setStagingBinCode(b.getBinCode()));
                }
                if (palletDto.getCurrentLocationId() != null) {
                    binRepository.findById(palletDto.getCurrentLocationId())
                            .ifPresent(b -> palletDto.setCurrentBinCode(b.getBinCode()));
                }
            }
        }
        return dto;
    }

    private String generateReceivingNumber() {
        return "RCV-" + System.currentTimeMillis();
    }

    private String generatePutawayTaskNumber() {
        return "TSK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
