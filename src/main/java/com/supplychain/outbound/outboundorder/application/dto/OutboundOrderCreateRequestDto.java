package com.supplychain.outbound.outboundorder.application.dto;

import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OutboundOrderCreateRequestDto {

    @NotNull(message = "Order date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate orderDate;

    @NotNull(message = "Order type is required")
    private OutboundOrderType orderType;

    // Customer – must reference an existing, ACTIVE Customer Master record
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    /** ID of an existing, active Location serving as the origin (dispatch) point */
    @NotNull(message = "Origin location ID is required")
    private Long originLocationId;

    /** ID of an existing, active Location serving as the destination (delivery) point */
    @NotNull(message = "Destination location ID is required")
    private Long destinationLocationId;

    // Pick Time Window
    private LocalDateTime pickTimeWindowStart;
    private LocalDateTime pickTimeWindowEnd;

    // Delivery Time Window
    private LocalDateTime deliveryTimeWindowStart;
    private LocalDateTime deliveryTimeWindowEnd;

    private String currencyCode;

    private String instructions;

    private String notes;

    @Valid
    @NotEmpty(message = "At least one order line is required")
    private List<OutboundOrderLineRequestDto> lines = new ArrayList<>();
}
