package com.supplychain.wms.receiving.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ReceivingPalletRequestDto {

    @NotNull(message = "ASN line ID is required")
    private Long asnLineId;

    @NotNull(message = "Receiving quantity is required")
    @DecimalMin(value = "0.0001", message = "Receiving quantity must be greater than 0")
    private BigDecimal quantity;

    private String condition;

    @NotNull(message = "Staging location (bin) ID is required")
    private Long stagingLocationId;

    private Long palletMasterId;
    private String palletNumber;
    private String lpnNumber;
    private String batchNumber;
    private String serialNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
}
