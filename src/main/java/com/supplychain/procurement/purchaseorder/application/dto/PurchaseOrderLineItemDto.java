package com.supplychain.procurement.purchaseorder.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import com.supplychain.procurement.purchaseorder.domain.enums.LineStatus;
import com.supplychain.procurement.purchaseorder.domain.enums.LineType;
import com.supplychain.procurement.purchaseorder.domain.enums.ProductClass;
import com.supplychain.procurement.purchaseorder.domain.enums.ProtectionLevelCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseOrderLineItemDto extends BaseDto {

    // 1. Line Number
    @NotNull(message = "Line number is required")
    private Integer lineNumber;

    // 2. Line Type
    @NotNull(message = "Line type is required")
    private LineType lineType;

    // 3. Item Code
    @NotBlank(message = "Item code is required")
    private String itemCode;

    // 4. Item Name
    @NotBlank(message = "Item name is required")
    private String itemName;

    // 5. Product Class
    @NotNull(message = "Product class is required")
    private ProductClass productClass;

    // 6. Supplier Item Code
    private String supplierItemCode;

    // 7. Item GTIN
    private String itemGtin;

    // 8. Quantity Ordered
    @NotNull(message = "Quantity ordered is required")
    @DecimalMin(value = "0.0001", message = "Quantity ordered must be greater than 0")
    private BigDecimal quantityOrdered;

    // 9. Quantity Received
    private BigDecimal quantityReceived;

    // 10. Shipped Quantity
    private BigDecimal shippedQuantity;

    // 11. Original Order Quantity
    @NotNull(message = "Original order quantity is required")
    @DecimalMin(value = "0.0001", message = "Original order quantity must be greater than 0")
    private BigDecimal originalOrderQuantity;

    // 12. Quantity UOM
    @NotBlank(message = "Quantity UOM is required")
    private String quantityUom;

    // 13. Unit Price
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    // 14. Currency Code
    @NotBlank(message = "Currency code is required")
    private String currencyCode;

    // 15. Total Monetary Value
    private BigDecimal totalMonetaryValue;

    // 16. Total Weight
    private BigDecimal totalWeight;

    // 17. Weight UOM
    private String weightUom;

    // 18. Total Volume
    private BigDecimal totalVolume;

    // 19. Volume UOM
    private String volumeUom;

    // 20. Protection Level Code
    private ProtectionLevelCode protectionLevelCode;

    // 21. Planned Size 1
    private String plannedSize1;

    // 22. Planned Size 2
    private String plannedSize2;

    // 23. Planned Weight
    private BigDecimal plannedWeight;

    // 24. Planned Volume
    private BigDecimal plannedVolume;

    // 25. Line Status
    @NotNull(message = "Line status is required")
    private LineStatus lineStatus;
}
