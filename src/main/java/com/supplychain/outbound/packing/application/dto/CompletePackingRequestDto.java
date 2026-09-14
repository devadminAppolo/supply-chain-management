package com.supplychain.outbound.packing.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CompletePackingRequestDto {

    @NotNull(message = "Quantity packed is required")
    @DecimalMin(value = "0.0001", message = "Quantity packed must be greater than zero")
    private BigDecimal quantityPacked;

    @NotBlank(message = "Package type is required")
    private String packageType;

    private BigDecimal weight;
    private String weightUom;

    private BigDecimal volume;
    private String volumeUom;

    private String notes;
}
