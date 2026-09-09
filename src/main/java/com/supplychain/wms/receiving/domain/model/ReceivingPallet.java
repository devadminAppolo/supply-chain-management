package com.supplychain.wms.receiving.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.procurement.asn.domain.model.AdvanceShippingNotice;
import com.supplychain.procurement.asn.domain.model.AdvanceShippingNoticeLine;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrder;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrderLineItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "receiving_pallets")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivingPallet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_id", nullable = false)
    private Receiving receiving;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asn_id", nullable = false)
    private AdvanceShippingNotice advanceShippingNotice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asn_line_id", nullable = false)
    private AdvanceShippingNoticeLine advanceShippingNoticeLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_line_id")
    private PurchaseOrderLineItem purchaseOrderLine;

    @Column(name = "pallet_master_id")
    private Long palletMasterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pallet_master_id", insertable = false, updatable = false)
    private com.supplychain.wms.pallet.domain.model.Pallet palletMaster;

    @Column(name = "pallet_number", nullable = false)
    private String palletNumber;

    @Column(name = "lpn_number")
    private String lpnNumber;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "uom_id")
    private Long uomId;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Builder.Default
    @Column(name = "condition")
    private String condition = "GOOD";

    @Column(name = "staging_bin_id", nullable = false)
    private Long stagingBinId;

    @Column(name = "current_bin_id")
    private Long currentBinId;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "pallet_status", nullable = false)
    private String palletStatus;
}
