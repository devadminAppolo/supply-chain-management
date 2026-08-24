package com.supplychain.wms.inventory.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import com.supplychain.common.domain.enums.InventoryStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class InventoryDto extends BaseDto {

    @NotBlank(message = "Warehouse is required")
    private String warehouseId;

    @NotBlank(message = "Bin is required")
    private String binId;

    @NotBlank(message = "Product is required")
    private String productId;

    @NotBlank(message = "SKU is required")
    private String skuId;

    @NotBlank(message = "UOM is required")
    private String uomId;

    private String lotNumber;

    private String serialNumber;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;

    @NotNull(message = "Inventory status is required")
    private InventoryStatus inventoryStatus;

    @NotNull(message = "On-hand quantity is required")
    @DecimalMin(value = "0.0", message = "On-hand quantity must be >= 0")
    private BigDecimal onHandQuantity;

    @NotNull(message = "Reserved quantity is required")
    @DecimalMin(value = "0.0", message = "Reserved quantity must be >= 0")
    private BigDecimal reservedQuantity;

    @NotNull(message = "Allocated quantity is required")
    @DecimalMin(value = "0.0", message = "Allocated quantity must be >= 0")
    private BigDecimal allocatedQuantity;

    @NotNull(message = "Available quantity is required")
    @DecimalMin(value = "0.0", message = "Available quantity must be >= 0")
    private BigDecimal availableQuantity;
}
