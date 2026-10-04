package com.supplychain.outbound.transportorder.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import com.supplychain.outbound.transportorder.domain.enums.TransportOrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TransportOrderResponseDto extends BaseDto {

    // Business key
    private String toNumber;

    // Status
    private TransportOrderStatus toStatus;

    // Transportation details
    private String mode;
    private String serviceLevelName;
    private String notes;

    // Cross-reference strings (optional)
    private String poNumber;
    private String asnNumber;

    // Origin Location
    private Long originLocationId;
    private String originLocationCode;
    private String originLocationName;

    // Destination Location
    private Long destinationLocationId;
    private String destinationLocationCode;
    private String destinationLocationName;

    // Pickup Window
    private LocalDateTime pickUpFrom;
    private LocalDateTime pickUpTo;

    // Delivery Window
    private LocalDateTime deliveryFrom;
    private LocalDateTime deliveryTo;

    // Associated Outbound Orders
    private List<TransportOrderOutboundOrderResponseDto> outboundOrders = new ArrayList<>();

    // Line items derived from OO lines
    private List<TransportOrderLineItemResponseDto> lineItems = new ArrayList<>();
}
