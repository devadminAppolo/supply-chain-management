package com.supplychain.procurement.asn.application.dto;

import com.supplychain.procurement.purchaseorder.domain.enums.PoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailablePurchaseOrderDto {

    private Long id;
    private String poNumber;
    private LocalDate poDate;
    private PoStatus poStatus;
    private String supplier;
    private String warehouse;
    private String distributionCenter;
    private String currencyCode;
    private int totalLines;
    private int shippableLinesCount;
}
