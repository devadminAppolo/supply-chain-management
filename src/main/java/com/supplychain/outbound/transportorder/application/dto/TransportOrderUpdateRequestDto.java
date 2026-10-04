package com.supplychain.outbound.transportorder.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Fields that may be updated on a Transport Order.
 *
 * <p>Updates are only permitted while the TO is in DRAFT or PENDING status.
 * The associated Outbound Orders and line items are not modifiable through
 * the update endpoint in Phase 1; they are fixed at creation time.
 */
@Getter
@Setter
public class TransportOrderUpdateRequestDto {

    @NotNull(message = "Destination location is required")
    private Long destinationLocationId;

    @NotBlank(message = "Service level name is required")
    private String serviceLevelName;

    @NotBlank(message = "Mode is required")
    private String mode;

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
