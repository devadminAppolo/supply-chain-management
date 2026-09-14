package com.supplychain.outbound.outboundorder.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OutboundOrderLineResponseDto extends BaseDto {

    private Integer lineNumber;

    private Long skuId;
    private String skuCode;
    private String skuName;

    private BigDecimal quantityOrdered;
    private String quantityUom;

    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String currencyCode;

    private BigDecimal totalWeight;
    private String weightUom;

    private BigDecimal totalVolume;
    private String volumeUom;
}
