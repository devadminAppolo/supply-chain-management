package com.supplychain.outbound.transportorder.domain.model;

import com.supplychain.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Explicit association between a Transport Order and an Outbound Order.
 *
 * <p>One TO may contain many Outbound Orders.
 * One Outbound Order may only belong to one active (non-cancelled) TO at a time.
 * This invariant is enforced at the service layer (pessimistic lock check) and
 * at the database level via the partial unique index
 * {@code uidx_to_outbound_order_pair}.
 */
@Entity
@Table(name = "transport_order_outbound_orders")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransportOrderOutboundOrder extends BaseEntity {

    // Parent TO reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_order_id", nullable = false)
    private TransportOrder transportOrder;

    // Scalar FK to Outbound Order (no bidirectional association needed here)
    @Column(name = "outbound_order_id", nullable = false)
    private Long outboundOrderId;
}
