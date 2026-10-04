package com.supplychain.outbound.transportorder.infrastructure.repository;

import com.supplychain.outbound.transportorder.domain.model.TransportOrderLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportOrderLineItemRepository extends JpaRepository<TransportOrderLineItem, Long> {

    List<TransportOrderLineItem> findAllByTransportOrderIdAndDeletedFalse(Long transportOrderId);
}
