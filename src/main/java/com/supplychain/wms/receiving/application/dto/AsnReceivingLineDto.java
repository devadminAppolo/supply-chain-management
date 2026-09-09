package com.supplychain.wms.receiving.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsnReceivingLineDto {
    private Long asnLineId;
    private Long poLineId;
    private Integer lineNumber;
    private Long productId;
    private Long skuId;
    private Long uomId;
    private String itemCode;
    private String itemName;
    private String supplierItemCode;
    private String itemGtin;
    private String batchNumber;
    private String serialNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private BigDecimal quantityOrdered;
    private BigDecimal asnQuantity;
    private BigDecimal previouslyReceivedQuantity;
    private BigDecimal remainingQuantity;
    private String uom;
    private BigDecimal unitPrice;
    private String currencyCode;
    private String lineStatus;
}
