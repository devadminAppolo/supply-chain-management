package com.supplychain.outbound.picking.domain.enums;

/**
 * Lifecycle of a backend-generated picking task.
 *
 * Normal flow: PENDING → ASSIGNED → IN_PROGRESS → COMPLETED
 *
 * Invalid transitions are rejected server-side.
 */
public enum PickingStatus {

    /** Task created by the system, awaiting staff assignment */
    PENDING,

    /** Staff has been assigned, task not yet started */
    ASSIGNED,

    /** Staff has started picking */
    IN_PROGRESS,

    /** All required quantity has been picked */
    COMPLETED,

    /** Task cancelled – inventory allocation is reversed */
    CANCELLED
}
