package com.supplychain.outbound.outboundorder.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderStatus;
import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OutboundOrderResponseDto extends BaseDto {

    private String orderNumber;
    private LocalDate orderDate;
    private OutboundOrderType orderType;
    private OutboundOrderStatus orderStatus;

    // Customer
    private String customerCode;
    private String customerName;

    // Origin Location
    private Long originLocationId;
    private String originLocationCode;
    private String originLocationName;

    // Destination Location
    private Long destinationLocationId;
    private String destinationLocationCode;
    private String destinationLocationName;

    // Time Windows
    private LocalDateTime pickTimeWindowStart;
    private LocalDateTime pickTimeWindowEnd;
    private LocalDateTime deliveryTimeWindowStart;
    private LocalDateTime deliveryTimeWindowEnd;

    // Financials
    private BigDecimal totalAmount;
    private String currencyCode;

    // Instructions
    private String instructions;
    private String notes;

    // Child lines
    private List<OutboundOrderLineResponseDto> lines = new ArrayList<>();
}
