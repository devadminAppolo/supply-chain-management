package com.supplychain.outbound.outboundorder.infrastructure.repository;

import com.supplychain.outbound.outboundorder.domain.enums.OutboundOrderStatus;
import com.supplychain.outbound.outboundorder.domain.model.OutboundOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutboundOrderRepository
        extends JpaRepository<OutboundOrder, Long>, JpaSpecificationExecutor<OutboundOrder> {

    boolean existsByOrderNumberAndDeletedFalse(String orderNumber);

    Optional<OutboundOrder> findByIdAndDeletedFalse(Long id);

    Optional<OutboundOrder> findByOrderNumberAndDeletedFalse(String orderNumber);

    List<OutboundOrder> findAllByDeletedFalseOrderByOrderNumberAsc();

    List<OutboundOrder> findAllByOrderStatusAndDeletedFalse(OutboundOrderStatus orderStatus);

    /**
     * Acquires a PESSIMISTIC_WRITE lock on the order row.
     * Used during picking/packing completion to prevent duplicate
     * status transitions and duplicate packing-task creation under concurrency.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OutboundOrder o WHERE o.id = :id AND o.deleted = false")
    Optional<OutboundOrder> findByIdAndDeletedFalseWithLock(@Param("id") Long id);
}
