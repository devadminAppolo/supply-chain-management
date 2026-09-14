package com.supplychain.outbound.packing.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.outbound.packing.domain.enums.PackingStatus;
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
@Table(name = "packing_tasks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PackingTask extends BaseEntity {

    // 1. Business keys – backend-generated
    @Column(name = "packing_number", nullable = false)
    private String packingNumber;

    /** Assigned at completion time once the physical package is sealed. */
    @Column(name = "package_id")
    private String packageId;

    // 2. Parent document reference
    @Column(name = "outbound_order_id", nullable = false)
    private Long outboundOrderId;

    // 3. Package type (e.g. BOX, PALLET, ENVELOPE)
    @Column(name = "package_type")
    private String packageType;

    // 4. Quantity tracking
    @Column(name = "quantity_to_pack", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityToPack;

    @Builder.Default
    @Column(name = "quantity_packed", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityPacked = BigDecimal.ZERO;

    @Column(name = "quantity_uom")
    private String quantityUom;

    // 5. Physical dimensions (set at completion)
    @Column(name = "weight", precision = 19, scale = 4)
    private BigDecimal weight;

    @Column(name = "weight_uom")
    private String weightUom;

    @Column(name = "volume", precision = 19, scale = 4)
    private BigDecimal volume;

    @Column(name = "volume_uom")
    private String volumeUom;

    // 6. Staff assignment (stored as String, consistent with PutawayTask / PickingTask)
    @Column(name = "assigned_staff_id")
    private String assignedStaffId;

    @Column(name = "assigned_staff_name")
    private String assignedStaffName;

    // 7. Status lifecycle
    @Enumerated(EnumType.STRING)
    @Column(name = "packing_status", nullable = false)
    private PackingStatus packingStatus;

    // 8. Workflow timestamps
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
