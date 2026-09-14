package com.supplychain.outbound.outboundorder.domain.enums;

/**
 * Full outbound-order lifecycle.
 *
 * Phase 1 uses only: DRAFT, PENDING, CONFIRMED, CANCELLED.
 * The remaining statuses are reserved for future allocation /
 * picking / packing / shipment phases and must not be set by
 * Phase 1 business logic.
 */
public enum OutboundOrderStatus {

    // --------------------------------------------------------
    // Phase 1 – active statuses
    // --------------------------------------------------------

    /** Order created but not yet submitted for processing */
    DRAFT,

    /** Order submitted; awaiting warehouse confirmation */
    PENDING,

    /** Order confirmed by the warehouse; ready for allocation */
    CONFIRMED,

    /** Order cancelled before shipment */
    CANCELLED,

    // --------------------------------------------------------
    // Future phases – DO NOT use in Phase 1 service logic
    // --------------------------------------------------------

    ALLOCATED,
    PARTIALLY_ALLOCATED,
    PICKING,
    PARTIALLY_PICKED,
    PICKED,
    PACKING,
    PARTIALLY_PACKED,
    PACKED,
    READY_TO_SHIP,
    SHIPPED,
    COMPLETED
}
