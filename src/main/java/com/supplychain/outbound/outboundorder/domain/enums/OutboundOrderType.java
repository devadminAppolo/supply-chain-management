package com.supplychain.outbound.outboundorder.domain.enums;

/**
 * Classifies the purpose or channel of an outbound order.
 * Only Phase 1 statuses are actively transitioned; future phases
 * may introduce additional workflow behaviour per type.
 */
public enum OutboundOrderType {

    /** Standard customer fulfilment order */
    STANDARD,

    /** Expedited/same-day fulfilment order */
    EXPRESS,

    /** High-priority order overriding normal wave planning */
    PRIORITY,

    /** Internal stock transfer between two warehouses */
    TRANSFER,

    /** Customer-initiated return / reverse logistics */
    RETURN,

    /** Fulfilment of previously back-ordered demand */
    BACKORDER,

    /** Sample shipment for marketing or QA purposes */
    SAMPLE,

    /** Replacement for a damaged or incorrect shipment */
    REPLACEMENT
}
