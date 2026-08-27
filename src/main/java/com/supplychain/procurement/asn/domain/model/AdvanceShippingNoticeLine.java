package com.supplychain.procurement.asn.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.procurement.asn.domain.enums.AsnLineStatus;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrderLineItem;
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
import java.time.LocalDate;

@Entity
@Table(name = "advance_shipping_notice_lines")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AdvanceShippingNoticeLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asn_id", nullable = false)
    private AdvanceShippingNotice advanceShippingNotice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_line_id", nullable = false)
    private PurchaseOrderLineItem purchaseOrderLine;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "supplier_item_code")
    private String supplierItemCode;

    @Column(name = "item_gtin")
    private String itemGtin;

    @Column(name = "quantity_ordered", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityOrdered;

    @Column(name = "previously_shipped_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal previouslyShippedQuantity;

    @Column(name = "shipment_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal shipmentQuantity;

    @Column(name = "remaining_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal remainingQuantity;

    @Column(name = "quantity_uom", nullable = false)
    private String quantityUom;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "total_monetary_value", precision = 19, scale = 4)
    private BigDecimal totalMonetaryValue;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "country_of_origin")
    private String countryOfOrigin;

    @Column(name = "weight", precision = 19, scale = 4)
    private BigDecimal weight;

    @Column(name = "weight_uom")
    private String weightUom;

    @Column(name = "volume", precision = 19, scale = 4)
    private BigDecimal volume;

    @Column(name = "volume_uom")
    private String volumeUom;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_status", nullable = false)
    private AsnLineStatus lineStatus;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
