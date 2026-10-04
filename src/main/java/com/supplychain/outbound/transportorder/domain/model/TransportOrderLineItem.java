package com.supplychain.outbound.transportorder.domain.model;

import com.supplychain.common.domain.BaseEntity;
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

@Entity
@Table(name = "transport_order_line_items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransportOrderLineItem extends BaseEntity {

    // Parent reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_order_id", nullable = false)
    private TransportOrder transportOrder;

    // 1. Traceability – preserved references back to source OO and OO line (optional for manual TOs)
    @Column(name = "outbound_order_id")
    private Long outboundOrderId;

    @Column(name = "outbound_order_line_id")
    private Long outboundOrderLineId;

    // 2. Line identity (sequential within TO)
    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    // 3. SKU Reference (FK + snapshot at creation time)
    @Column(name = "sku_id")
    private Long skuId;

    @Column(name = "sku_code")
    private String skuCode;

    @Column(name = "sku_name")
    private String skuName;

    // 4. Quantity
    @Column(name = "quantity_ordered", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityOrdered;

    @Column(name = "quantity_uom")
    private String quantityUom;

    // 5. Weight (carried from OO Line)
    @Column(name = "total_weight", precision = 19, scale = 4)
    private BigDecimal totalWeight;

    @Builder.Default
    @Column(name = "weight_uom")
    private String weightUom = "KG";

    // 6. Volume (carried from OO Line)
    @Column(name = "total_volume", precision = 19, scale = 4)
    private BigDecimal totalVolume;

    @Builder.Default
    @Column(name = "volume_uom")
    private String volumeUom = "CBM";
}
