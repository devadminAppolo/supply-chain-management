package com.supplychain.procurement.purchaseorder.domain.model;

import com.supplychain.common.domain.BaseEntity;
import com.supplychain.procurement.purchaseorder.domain.enums.LineStatus;
import com.supplychain.procurement.purchaseorder.domain.enums.LineType;
import com.supplychain.procurement.purchaseorder.domain.enums.ProductClass;
import com.supplychain.procurement.purchaseorder.domain.enums.ProtectionLevelCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "purchase_order_line_items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderLineItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    // 1. Line Number
    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    // 2. Line Type
    @Enumerated(EnumType.STRING)
    @Column(name = "line_type", nullable = false)
    private LineType lineType;

    // 3. Item Code
    @Column(name = "item_code", nullable = false)
    private String itemCode;

    // 4. Item Name
    @Column(name = "item_name", nullable = false)
    private String itemName;

    // 5. Product Class
    @Enumerated(EnumType.STRING)
    @Column(name = "product_class", nullable = false)
    private ProductClass productClass;

    // 6. Supplier Item Code
    @Column(name = "supplier_item_code")
    private String supplierItemCode;

    // 7. Item GTIN
    @Column(name = "item_gtin")
    private String itemGtin;

    // 8. Quantity Ordered
    @Column(name = "quantity_ordered", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityOrdered;

    // 9. Quantity Received
    @Builder.Default
    @Column(name = "quantity_received", precision = 19, scale = 4)
    private BigDecimal quantityReceived = BigDecimal.ZERO;

    // 10. Shipped Quantity
    @Builder.Default
    @Column(name = "shipped_quantity", precision = 19, scale = 4)
    private BigDecimal shippedQuantity = BigDecimal.ZERO;

    // 11. Original Order Quantity
    @Column(name = "original_order_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalOrderQuantity;

    // 12. Quantity UOM
    @Column(name = "quantity_uom", nullable = false)
    private String quantityUom;

    // 13. Unit Price
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    // 14. Currency Code
    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    // 15. Total Monetary Value
    @Column(name = "total_monetary_value", precision = 19, scale = 4)
    private BigDecimal totalMonetaryValue;

    // 16. Total Weight
    @Column(name = "total_weight", precision = 19, scale = 4)
    private BigDecimal totalWeight;

    // 17. Weight UOM
    @Column(name = "weight_uom")
    private String weightUom;

    // 18. Total Volume
    @Column(name = "total_volume", precision = 19, scale = 4)
    private BigDecimal totalVolume;

    // 19. Volume UOM
    @Column(name = "volume_uom")
    private String volumeUom;

    // 20. Protection Level Code
    @Enumerated(EnumType.STRING)
    @Column(name = "protection_level_code")
    private ProtectionLevelCode protectionLevelCode;

    // 21. Planned Size 1
    @Column(name = "planned_size1")
    private String plannedSize1;

    // 22. Planned Size 2
    @Column(name = "planned_size2")
    private String plannedSize2;

    // 23. Planned Weight
    @Column(name = "planned_weight", precision = 19, scale = 4)
    private BigDecimal plannedWeight;

    // 24. Planned Volume
    @Column(name = "planned_volume", precision = 19, scale = 4)
    private BigDecimal plannedVolume;

    // 25. Line Status
    @Enumerated(EnumType.STRING)
    @Column(name = "line_status", nullable = false)
    private LineStatus lineStatus;
}
