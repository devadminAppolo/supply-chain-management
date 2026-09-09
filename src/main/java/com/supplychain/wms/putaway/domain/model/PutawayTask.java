package com.supplychain.wms.putaway.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.procurement.asn.domain.model.AdvanceShippingNotice;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrder;
import com.supplychain.wms.location.domain.model.Warehouse;
import com.supplychain.wms.putaway.domain.enums.PutawayStatus;
import com.supplychain.wms.receiving.domain.model.Receiving;
import com.supplychain.wms.receiving.domain.model.ReceivingPallet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "putaway_tasks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PutawayTask extends BaseEntity {

    @Column(name = "task_number", nullable = false)
    private String taskNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_id", nullable = false)
    private Receiving receiving;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pallet_id", nullable = false)
    private ReceivingPallet pallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asn_id", nullable = false)
    private AdvanceShippingNotice advanceShippingNotice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "uom_id")
    private Long uomId;

    @Column(name = "item_code")
    private String itemCode;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "staging_bin_id", nullable = false)
    private Long stagingBinId;

    @Column(name = "putaway_bin_id")
    private Long putawayBinId;

    @Column(name = "assigned_staff_id")
    private String assignedStaffId;

    @Column(name = "assigned_staff_name")
    private String assignedStaffName;

    @Enumerated(EnumType.STRING)
    @Column(name = "putaway_status", nullable = false)
    private PutawayStatus putawayStatus;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "notes")
    private String notes;
}
