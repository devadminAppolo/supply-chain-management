package com.supplychain.wms.putaway.infrastructure.repository;

import com.supplychain.wms.putaway.domain.enums.PutawayStatus;
import com.supplychain.wms.putaway.domain.model.PutawayTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PutawayTaskRepository extends JpaRepository<PutawayTask, Long>, JpaSpecificationExecutor<PutawayTask> {

    boolean existsByTaskNumberAndDeletedFalse(String taskNumber);

    Optional<PutawayTask> findByIdAndDeletedFalse(Long id);

    Optional<PutawayTask> findByTaskNumberAndDeletedFalse(String taskNumber);

    List<PutawayTask> findByReceivingIdAndDeletedFalse(Long receivingId);

    List<PutawayTask> findByPalletIdAndDeletedFalse(Long palletId);

    boolean existsByPalletIdAndPutawayStatusInAndDeletedFalse(Long palletId, Collection<PutawayStatus> statuses);

    Page<PutawayTask> findAllByDeletedFalse(Pageable pageable);

    Page<PutawayTask> findByPutawayStatusAndDeletedFalse(PutawayStatus status, Pageable pageable);

    Page<PutawayTask> findByWarehouseIdAndDeletedFalse(Long warehouseId, Pageable pageable);
}
