package com.supplychain.procurement.asn.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import com.supplychain.procurement.asn.domain.enums.AsnStatus;
import com.supplychain.procurement.purchaseorder.domain.enums.Priority;
import com.supplychain.procurement.purchaseorder.domain.enums.ShippingMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AsnResponseDto extends BaseDto {

    private String asnNumber;
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
    private String vehicleNumber;
    private ShippingMethod shippingMethod;
    private Priority priority;
    private String packingSlipNumber;
    private String billOfLadingNumber;
    private String containerNumber;
    private Integer totalPackages;
    private BigDecimal totalWeight;
    private String weightUom;
    private BigDecimal totalVolume;
    private String volumeUom;
    private AsnStatus asnStatus;
    private String specialInstructions;
    private String notes;

    private List<AsnLineResponseDto> lines = new ArrayList<>();
}
