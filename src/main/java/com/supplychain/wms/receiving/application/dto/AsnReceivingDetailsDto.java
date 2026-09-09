package com.supplychain.wms.receiving.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsnReceivingDetailsDto {
    private Long asnId;
    private String asnNumber;
    private String asnStatus;
    private Long purchaseOrderId;
    private String purchaseOrderNumber;
    private String supplierId;
    private String supplierName;
    private String warehouseId;
    private String warehouseName;
    private LocalDate shipmentDate;
    private LocalDate expectedArrivalDate;
    private String carrierId;
    private String carrierName;
    private String trackingNumber;
    private String containerNumber;
    private String vehicleNumber;
    @Builder.Default
    private List<AsnReceivingLineDto> lines = new ArrayList<>();
}
