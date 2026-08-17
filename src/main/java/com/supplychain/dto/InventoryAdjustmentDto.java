package com.supplychain.dto;

import com.supplychain.enums.AdjustmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class InventoryAdjustmentDto extends BaseDto {

    // BaseDto already provides: id, orgId, dcId, status, remarks,
    // createdBy, createdDate, modifiedBy, modifiedDate, deleted

    @NotBlank(message = "Warehouse is required")
    private String warehouseId;

    @NotBlank(message = "SKU is required")
    private String skuId;

    @NotBlank(message = "Bin is required")
    private String binId;

    @NotBlank(message = "UOM is required")
    private String uomId;

    @NotNull(message = "Adjustment type is required")
    private AdjustmentType adjustmentType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    private String reasonCode;

    private String referenceNumber;

//    @NotNull(message = "Adjustment date is required")
    private LocalDateTime adjustmentDate;
}
