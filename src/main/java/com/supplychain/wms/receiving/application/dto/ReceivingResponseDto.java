package com.supplychain.wms.receiving.application.dto;

import com.supplychain.wms.receiving.domain.enums.ReceivingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivingResponseDto {
    private Long id;
    private String receivingNumber;
    private Long asnId;
    private String asnNumber;
    private Long purchaseOrderId;
    private String purchaseOrderNumber;
    private String supplierId;
    private String supplierName;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private LocalDateTime arrivalTime;
    private LocalDateTime receivingDate;
    private ReceivingStatus receivingStatus;
    private String notes;
    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;
    @Builder.Default
    private List<ReceivingPalletResponseDto> pallets = new ArrayList<>();
}
