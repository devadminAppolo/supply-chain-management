package com.supplychain.outbound.outboundorder.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OutboundOrderLineUpdateRequestDto {

    /**
     * When provided and matches an existing line ID, the existing line is updated.
     * When null or unrecognised, a new line is created.
     */
    private Long id;

    @NotNull(message = "SKU ID is required")
    private Long skuId;

    @NotNull(message = "Quantity ordered is required")
    @DecimalMin(value = "0.0001", message = "Quantity ordered must be greater than zero")
    private BigDecimal quantityOrdered;

    private String quantityUom;

    private BigDecimal unitPrice;

    private String currencyCode;
}
