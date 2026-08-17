package com.supplychain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class BinTransferDto extends BaseDto {

    // BaseDto already provides: id, orgId, dcId, status, remarks,
    // createdBy, createdDate, modifiedBy, modifiedDate, deleted

    @NotBlank(message = "Source warehouse is required")
    private String sourceWarehouseId;       // warehouse name

    @NotBlank(message = "Source bin is required")
    private String sourceBinId;             // bin code

    @NotBlank(message = "Destination warehouse is required")
    private String destinationWarehouseId;  // warehouse name

    @NotBlank(message = "Destination bin is required")
    private String destinationBinId;        // bin code

    @NotBlank(message = "SKU is required")
    private String skuId;                   // SKU code

    @NotBlank(message = "UOM is required")
    private String uomId;                   // UOM name

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    private String reasonCode;

    private LocalDateTime transferDate;
}
