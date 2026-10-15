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

/**
 * Update is only permitted when order status is DRAFT or PENDING.
 * Order number and status are immutable via this request.
 */
@Getter
@Setter
public class OutboundOrderUpdateRequestDto {

    @NotNull(message = "Order date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate orderDate;

    @NotNull(message = "Order type is required")
    private OutboundOrderType orderType;

    // Customer – must reference an existing, ACTIVE Customer Master record
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Origin location ID is required")
    private Long originLocationId;

    @NotNull(message = "Destination location ID is required")
    private Long destinationLocationId;

    private LocalDateTime pickTimeWindowStart;
    private LocalDateTime pickTimeWindowEnd;

    private LocalDateTime deliveryTimeWindowStart;
    private LocalDateTime deliveryTimeWindowEnd;

    private String currencyCode;

    private String instructions;

    private String notes;

    @Valid
    @NotEmpty(message = "At least one order line is required")
    private List<OutboundOrderLineUpdateRequestDto> lines = new ArrayList<>();
}
