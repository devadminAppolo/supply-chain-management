package com.supplychain.procurement.asn.application.dto;

import com.supplychain.procurement.purchaseorder.domain.enums.Priority;
import com.supplychain.procurement.purchaseorder.domain.enums.ShippingMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AsnCreateRequestDto {

    private String asnNumber;

    @NotNull(message = "Purchase order ID is required")
    private Long purchaseOrderId;

    private String supplierId;
    private String supplierName;
    private String warehouseId;
    private String warehouseName;

    @NotNull(message = "Shipment date is required")
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
    private String specialInstructions;
    private String notes;

    @Valid
    @NotEmpty(message = "ASN must contain at least one line item")
    private List<AsnLineRequestDto> lines = new ArrayList<>();
}
