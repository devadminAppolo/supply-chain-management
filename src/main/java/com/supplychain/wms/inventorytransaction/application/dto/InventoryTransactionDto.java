package com.supplychain.wms.inventorytransaction.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import com.supplychain.common.domain.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class InventoryTransactionDto extends BaseDto {

    @NotBlank(message = "Warehouse is required")
    private String warehouseId;

    @NotNull(message = "Inventory is required")
    private Long inventoryId;

    @NotBlank(message = "Product is required")
    private String productId;

    @NotBlank(message = "SKU is required")
    private String skuId;

    @NotBlank(message = "UOM is required")
    private String uomId;

    private String fromBinId;

    private String toBinId;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    private String lotNumber;

    private String serialNumber;

    private String referenceType;

    private String referenceId;

    private String reasonCode;

    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDate;

    private BigDecimal quantityBefore;

    private BigDecimal quantityAfter;
}
