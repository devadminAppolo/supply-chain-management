package com.supplychain.outbound.packing.infrastructure.repository;

import com.supplychain.outbound.packing.domain.enums.PackingStatus;
import com.supplychain.outbound.packing.domain.model.PackingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackingTaskRepository extends JpaRepository<PackingTask, Long>,
        JpaSpecificationExecutor<PackingTask> {

    Optional<PackingTask> findByIdAndDeletedFalse(Long id);

    boolean existsByPackingNumberAndDeletedFalse(String packingNumber);

    List<PackingTask> findAllByOutboundOrderIdAndDeletedFalse(Long outboundOrderId);

    List<PackingTask> findAllByOutboundOrderIdAndPackingStatusAndDeletedFalse(
            Long outboundOrderId, PackingStatus packingStatus);

    long countByOutboundOrderIdAndPackingStatusAndDeletedFalse(
            Long outboundOrderId, PackingStatus packingStatus);

    @Query("SELECT COUNT(t) FROM PackingTask t WHERE t.outboundOrderId = :orderId AND t.deleted = false")
    long countByOutboundOrderId(@Param("orderId") Long orderId);
}
