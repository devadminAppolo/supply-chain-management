package com.supplychain.outbound.transportorder.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TransportOrderCreateRequestDto {

    /**
     * IDs of the Outbound Orders to include in this Transport Order (for consolidation flow).
     * Optional if creating a manual Transport Order.
     */
    private List<Long> outboundOrderIds;

    /**
     * Origin location ID (required for manual Transport Orders, or resolved from Outbound Orders).
     */
    private Long originLocationId;

    /**
     * Line items for manual Transport Orders.
     */
    @Valid
    private List<TransportOrderLineItemRequestDto> lineItems = new ArrayList<>();

    @NotBlank(message = "Service level name is required")
    private String serviceLevelName;

    @NotBlank(message = "Mode is required")
    private String mode;

    // Origin and destination are resolved from the selected Outbound Orders.
    // originLocationId is taken from the common origin of the selected orders.
    // destinationLocationId must be explicitly provided.
    @NotNull(message = "Destination location is required")
    private Long destinationLocationId;

    @NotNull(message = "Pickup start is required")
    private LocalDateTime pickUpFrom;

    @NotNull(message = "Pickup end is required")
    private LocalDateTime pickUpTo;

    @NotNull(message = "Delivery start is required")
    private LocalDateTime deliveryFrom;

    @NotNull(message = "Delivery end is required")
    private LocalDateTime deliveryTo;

    private String notes;

    // Optional cross-reference strings
    // poNumber  : customer's purchase order reference that originated this shipment
    // asnNumber : outbound ASN reference sent to the consignee
    private String poNumber;

    private String asnNumber;
}
