package com.supplychain.outbound.picking.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.outbound.picking.domain.enums.PickingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "picking_tasks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PickingTask extends BaseEntity {

    // 1. Business key – backend-generated, never client-supplied
    @Column(name = "pick_number", nullable = false)
    private String pickNumber;

    // 2. Parent document references
    @Column(name = "outbound_order_id", nullable = false)
    private Long outboundOrderId;

    @Column(name = "outbound_order_line_id", nullable = false)
    private Long outboundOrderLineId;

    // 3. Item snapshot (taken at allocation time)
    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "sku_code")
    private String skuCode;

    @Column(name = "sku_name")
    private String skuName;

    // 4. Source inventory record (the exact inventory row being picked)
    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "source_bin_id")
    private Long sourceBinId;

    @Column(name = "source_bin_code")
    private String sourceBinCode;

    // 5. Quantity tracking (quantityToPick set at allocation; quantityPicked set on completion)
    @Column(name = "quantity_to_pick", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityToPick;

    @Builder.Default
    @Column(name = "quantity_picked", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityPicked = BigDecimal.ZERO;

    @Column(name = "quantity_uom")
    private String quantityUom;

    // 6. Staff assignment (stored as String, consistent with PutawayTask convention)
    @Column(name = "assigned_staff_id")
    private String assignedStaffId;

    @Column(name = "assigned_staff_name")
    private String assignedStaffName;

    // 7. Status lifecycle
    @Enumerated(EnumType.STRING)
    @Column(name = "picking_status", nullable = false)
    private PickingStatus pickingStatus;

    // 8. Workflow timestamps
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
