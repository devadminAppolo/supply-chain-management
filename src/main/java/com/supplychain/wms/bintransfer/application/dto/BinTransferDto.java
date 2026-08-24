package com.supplychain.wms.bintransfer.application.dto;

import com.supplychain.common.application.dto.BaseDto;
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

    @NotBlank(message = "Source warehouse is required")
    private String sourceWarehouseId;

    @NotBlank(message = "Source bin is required")
    private String sourceBinId;

    @NotBlank(message = "Destination warehouse is required")
    private String destinationWarehouseId;

    @NotBlank(message = "Destination bin is required")
    private String destinationBinId;

    @NotBlank(message = "SKU is required")
    private String skuId;

    @NotBlank(message = "UOM is required")
    private String uomId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    private String reasonCode;

    private LocalDateTime transferDate;
}
