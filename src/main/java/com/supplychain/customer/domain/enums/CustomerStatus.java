package com.supplychain.customer.domain.enums;

/**
 * Represents the lifecycle status of a Customer Master record.
 * Stored as a plain string in the inherited {@code status} column of BaseEntity.
 */
public enum CustomerStatus {
    ACTIVE,
    INACTIVE
}
