package com.supplychain.outbound.picking.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CompletePickingRequestDto {

    @NotNull(message = "Quantity picked is required")
    @DecimalMin(value = "0.0001", message = "Quantity picked must be greater than zero")
    private BigDecimal quantityPicked;

    private String notes;
}
