package com.supplychain.common.domain.enums;

public enum TransactionType {
    RECEIPT,
    TRANSFER,
    PUTAWAY,
    ADJUSTMENT_IN,
    ADJUSTMENT_OUT,
    DAMAGE,
    RETURN,
    /** Item picked from storage bin during outbound order fulfilment */
    PICKING,
    /** Packed shipment dispatched / inventory permanently leaves the warehouse */
    DISPATCH
}
