package com.supplychain.enums;

/**
 * Supported inventory transaction types.
 *
 * To add future types (PUTAWAY, PICK, PACK, SHIP, CYCLE_COUNT, ALLOCATE,
 * DEALLOCATE, etc.) simply append additional constants here — no structural
 * or architectural change is required.
 */
public enum TransactionType {
    RECEIPT,
    TRANSFER,
    ADJUSTMENT_IN,
    ADJUSTMENT_OUT,
    DAMAGE,
    RETURN
}
