package com.supplychain.procurement.asn.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import com.supplychain.procurement.asn.domain.enums.AsnLineStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AsnLineResponseDto extends BaseDto {

    private Long asnId;
    private Long poLineId;
    private Integer lineNumber;
    private String itemCode;
    private String itemName;
    private String supplierItemCode;
    private String itemGtin;
    private BigDecimal quantityOrdered;
    private BigDecimal previouslyShippedQuantity;
    private BigDecimal shipmentQuantity;
    private BigDecimal remainingQuantity;
    private String quantityUom;
    private BigDecimal unitPrice;
    private String currencyCode;
    private BigDecimal totalMonetaryValue;
    private String batchNumber;
    private String serialNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private String countryOfOrigin;
    private BigDecimal weight;
    private String weightUom;
    private BigDecimal volume;
    private String volumeUom;
    private AsnLineStatus lineStatus;
    private String notes;
}
