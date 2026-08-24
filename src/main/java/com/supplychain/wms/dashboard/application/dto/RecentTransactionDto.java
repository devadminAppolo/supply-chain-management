package com.supplychain.wms.dashboard.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecentTransactionDto {
    private final Long transactionId;
    private final String transactionType;
    private final String sku;
    private final String product;
    private final BigDecimal quantity;
    private final String fromBin;
    private final String toBin;
    private final LocalDateTime transactionDate;
}
