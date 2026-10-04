package com.supplychain.outbound.transportorder.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransportOrderLineItemResponseDto extends BaseDto {

    // Traceability
    private Long outboundOrderId;
    private Long outboundOrderLineId;

    // Line identity
    private Integer lineNumber;

    // SKU
    private Long skuId;
    private String skuCode;
    private String skuName;

    // Quantity
    private BigDecimal quantityOrdered;
    private String quantityUom;

    // Weight
    private BigDecimal totalWeight;
    private String weightUom;

    // Volume
    private BigDecimal totalVolume;
    private String volumeUom;
}
