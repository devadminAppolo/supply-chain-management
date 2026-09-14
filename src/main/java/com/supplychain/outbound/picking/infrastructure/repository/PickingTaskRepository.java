package com.supplychain.outbound.picking.infrastructure.repository;

import com.supplychain.outbound.picking.domain.enums.PickingStatus;
import com.supplychain.outbound.picking.domain.model.PickingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PickingTaskRepository
        extends JpaRepository<PickingTask, Long>, JpaSpecificationExecutor<PickingTask> {

    Optional<PickingTask> findByIdAndDeletedFalse(Long id);

    Optional<PickingTask> findByPickNumberAndDeletedFalse(String pickNumber);

    boolean existsByPickNumberAndDeletedFalse(String pickNumber);

    List<PickingTask> findAllByOutboundOrderIdAndDeletedFalse(Long outboundOrderId);

    List<PickingTask> findAllByOutboundOrderIdAndPickingStatusAndDeletedFalse(
            Long outboundOrderId, PickingStatus pickingStatus);

    List<PickingTask> findAllByOutboundOrderLineIdAndDeletedFalse(Long outboundOrderLineId);

    long countByOutboundOrderIdAndPickingStatusAndDeletedFalse(
            Long outboundOrderId, PickingStatus pickingStatus);
}
