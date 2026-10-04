package com.supplychain.outbound.transportorder.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransportOrderLineItemRequestDto {

    private Long skuId;
    private String skuCode;
    private String skuName;

    @NotNull(message = "Quantity ordered is required")
    @DecimalMin(value = "0.0001", message = "Quantity ordered must be greater than zero")
    private BigDecimal quantityOrdered;

    private String quantityUom;
    private BigDecimal totalWeight;
    private String weightUom;
    private BigDecimal totalVolume;
    private String volumeUom;

    // Optional references if linked to an outbound order
    private Long outboundOrderId;
    private Long outboundOrderLineId;
}
