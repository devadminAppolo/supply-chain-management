package com.supplychain.procurement.purchaseorder.domain.enums;

public enum PoStatus {
    DRAFT,
    SUBMITTED,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SENT_TO_SUPPLIER,
    SUPPLIER_CONFIRMED,
    PARTIALLY_CONFIRMED,
    PARTIALLY_SHIPPED,
    FULLY_SHIPPED,
    PARTIALLY_RECEIVED,
    RECEIVED,
    CANCELLED,
    CLOSED,
    ON_HOLD
}
