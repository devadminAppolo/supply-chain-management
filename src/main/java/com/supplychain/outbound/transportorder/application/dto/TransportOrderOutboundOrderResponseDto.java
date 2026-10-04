package com.supplychain.outbound.transportorder.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the association between a Transport Order and an Outbound Order
 * in the API response.
 */
@Getter
@Setter
public class TransportOrderOutboundOrderResponseDto {

    /** Association record ID */
    private Long id;

    /** The referenced Outbound Order ID */
    private Long outboundOrderId;
}
