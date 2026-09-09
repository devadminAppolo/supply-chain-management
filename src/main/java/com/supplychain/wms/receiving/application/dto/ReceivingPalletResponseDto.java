package com.supplychain.wms.receiving.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivingPalletResponseDto {
    private Long id;
    private Long receivingId;
    private Long asnId;
    private Long asnLineId;
    private Long purchaseOrderId;
    private Long poLineId;
    private Long palletMasterId;
    private String palletNumber;
    private String lpnNumber;
    private Long productId;
    private Long skuId;
    private Long uomId;
    private String itemCode;
    private String itemName;
    private BigDecimal quantity;
    private String condition;
    private Long stagingLocationId;
    private String stagingBinCode;
    private Long currentLocationId;
    private String currentBinCode;
    private String batchNumber;
    private String serialNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private LocalDateTime receivedAt;
    private String palletStatus;
}
