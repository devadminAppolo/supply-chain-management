package com.supplychain.outbound.transportorder.domain.enums;

/**
 * Transport Order lifecycle statuses for Phase 1.
 *
 * <p>Allowed transitions (Phase 1):
 * <pre>
 *   DRAFT  → PENDING    (manual advancement)
 *   DRAFT  → CANCELLED  (cancellation)
 *   PENDING → PLANNED   (manual advancement)
 *   PENDING → CANCELLED (cancellation)
 * </pre>
 *
 * <p>PLANNED and beyond are operational states; they cannot be
 * cancelled or freely edited. Planning, dispatch, and execution
 * workflows are out of scope for Phase 1.
 */
public enum TransportOrderStatus {

    /** TO created, not yet submitted for planning */
    DRAFT,

    /** TO submitted; awaiting transport planning */
    PENDING,

    /** TO planned; locked for editing */
    PLANNED,

    /** TO cancelled; Outbound Order associations become available again */
    CANCELLED
}
