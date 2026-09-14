package com.supplychain.outbound.outboundorder.domain.model;

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
@Table(name = "outbound_order_lines")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundOrderLine extends BaseEntity {

    // Parent reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_order_id", nullable = false)
    private OutboundOrder outboundOrder;

    // 1. Line Number – backend-assigned sequential integer
    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    // 2. SKU Reference (FK to skus table + snapshot)
    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "sku_code")
    private String skuCode;

    @Column(name = "sku_name")
    private String skuName;

    // 3. Quantity
    @Column(name = "quantity_ordered", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityOrdered;

    @Column(name = "quantity_uom")
    private String quantityUom;

    // 4. Pricing – backend-calculated
    @Column(name = "unit_price", precision = 19, scale = 4)
    private BigDecimal unitPrice;

    // totalPrice = quantityOrdered × unitPrice
    @Column(name = "total_price", precision = 19, scale = 4)
    private BigDecimal totalPrice;

    @Column(name = "currency_code")
    private String currencyCode;

    // 5. Weight – backend-calculated from SKU master weight × quantityOrdered
    @Column(name = "total_weight", precision = 19, scale = 4)
    private BigDecimal totalWeight;

    @Builder.Default
    @Column(name = "weight_uom")
    private String weightUom = "KG";

    // 6. Volume – backend-calculated from Product master volume × quantityOrdered
    @Column(name = "total_volume", precision = 19, scale = 4)
    private BigDecimal totalVolume;

    @Builder.Default
    @Column(name = "volume_uom")
    private String volumeUom = "CBM";
}
