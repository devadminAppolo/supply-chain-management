package com.supplychain.outbound.packing.application.dto;

import com.supplychain.outbound.packing.domain.enums.PackingStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PackingTaskResponseDto {

    private Long id;

    private String packingNumber;
    private String packageId;

    // Order context (enriched by service)
    private Long outboundOrderId;
    private String orderNumber;
    private String customerCode;
    private String customerName;

    // Package details
    private String packageType;

    // Quantity tracking
    private BigDecimal quantityToPack;
    private BigDecimal quantityPacked;
    private BigDecimal quantityRemaining;   // derived: quantityToPack - quantityPacked
    private String quantityUom;

    // Physical dimensions
    private BigDecimal weight;
    private String weightUom;
    private BigDecimal volume;
    private String volumeUom;

    // Staff assignment
    private String assignedStaffId;
    private String assignedStaffName;

    // Status lifecycle
    private PackingStatus packingStatus;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private String notes;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
