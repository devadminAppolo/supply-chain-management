package com.supplychain.outbound.transportorder.infrastructure.repository;

import com.supplychain.outbound.transportorder.domain.model.TransportOrderOutboundOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportOrderOutboundOrderRepository extends JpaRepository<TransportOrderOutboundOrder, Long> {

    List<TransportOrderOutboundOrder> findAllByTransportOrderIdAndDeletedFalse(Long transportOrderId);

    /**
     * Finds the active association for a given Outbound Order.
     *
     * <p>An association is considered active when it is not soft-deleted
     * and its parent Transport Order is also not soft-deleted and not CANCELLED.
     * Used to prevent assigning an Outbound Order to a second active TO.
     */
    @Query("SELECT a FROM TransportOrderOutboundOrder a " +
           "JOIN TransportOrder t ON t.id = a.transportOrder.id " +
           "WHERE a.outboundOrderId = :outboundOrderId " +
           "AND a.deleted = false " +
           "AND t.deleted = false " +
           "AND t.toStatus <> com.supplychain.outbound.transportorder.domain.enums.TransportOrderStatus.CANCELLED")
    Optional<TransportOrderOutboundOrder> findActiveAssociationByOutboundOrderId(
            @Param("outboundOrderId") Long outboundOrderId);
}
