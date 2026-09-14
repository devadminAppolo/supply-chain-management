package com.supplychain.outbound.outboundorder.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OutboundOrderLineRequestDto {

    /** ID of an existing, active SKU in the catalog */
    @NotNull(message = "SKU ID is required")
    private Long skuId;

    @NotNull(message = "Quantity ordered is required")
    @DecimalMin(value = "0.0001", message = "Quantity ordered must be greater than zero")
    private BigDecimal quantityOrdered;

    /** UOM code (e.g. EA, CS, KG) */
    private String quantityUom;

    private BigDecimal unitPrice;

    private String currencyCode;
}
