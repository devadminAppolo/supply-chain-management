package com.supplychain.outbound.transportorder.infrastructure.repository;

import com.supplychain.outbound.transportorder.domain.model.TransportOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransportOrderRepository
        extends JpaRepository<TransportOrder, Long>, JpaSpecificationExecutor<TransportOrder> {

    Optional<TransportOrder> findByIdAndDeletedFalse(Long id);

    boolean existsByToNumberAndDeletedFalse(String toNumber);
}
