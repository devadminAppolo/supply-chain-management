package com.supplychain.procurement.asn.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AsnLineUpdateRequestDto {

    private Long id;

    @NotNull(message = "PO line ID is required")
    private Long poLineId;

    @NotNull(message = "Shipment quantity is required")
    @DecimalMin(value = "0.0001", message = "Shipment quantity must be greater than 0")
    private BigDecimal shipmentQuantity;

    private String batchNumber;
    private String serialNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private String countryOfOrigin;
    private BigDecimal weight;
    private String weightUom;
    private BigDecimal volume;
    private String volumeUom;
    private String notes;
}
