package com.supplychain.outbound.outboundorder.infrastructure.repository;

import com.supplychain.outbound.outboundorder.domain.model.OutboundOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboundOrderLineRepository extends JpaRepository<OutboundOrderLine, Long> {

    List<OutboundOrderLine> findAllByOutboundOrderIdAndDeletedFalse(Long outboundOrderId);
}
