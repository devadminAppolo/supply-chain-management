package com.supplychain.procurement.asn.application.dto;

import com.supplychain.procurement.purchaseorder.domain.enums.LineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailablePurchaseOrderLineDto {

    private Long poLineId;
    private Integer lineNumber;
    private String itemCode;
    private String itemName;
    private String supplierItemCode;
    private String itemGtin;
    private BigDecimal orderedQuantity;
    private BigDecimal previouslyShippedQuantity;
    private BigDecimal remainingQuantity;
    private String quantityUom;
    private BigDecimal unitPrice;
    private String currencyCode;
    private LineStatus lineStatus;
}
