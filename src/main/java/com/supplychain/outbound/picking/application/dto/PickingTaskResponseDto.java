package com.supplychain.outbound.picking.application.dto;

import com.supplychain.outbound.picking.domain.enums.PickingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickingTaskResponseDto {

    private Long id;
    private String pickNumber;

    // Parent order context
    private Long outboundOrderId;
    private String orderNumber;
    private String customerCode;
    private String customerName;

    // Order line context
    private Long outboundOrderLineId;
    private Integer lineNumber;

    // Item
    private Long skuId;
    private String skuCode;
    private String skuName;

    // Source location
    private Long warehouseId;
    private Long sourceBinId;
    private String sourceBinCode;

    // Quantities
    private BigDecimal quantityToPick;
    private BigDecimal quantityPicked;
    private BigDecimal quantityRemaining;
    private String quantityUom;

    // Assignment
    private String assignedStaffId;
    private String assignedStaffName;

    // Lifecycle
    private PickingStatus pickingStatus;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
