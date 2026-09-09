package com.supplychain.wms.receiving.infrastructure.repository;

import com.supplychain.wms.receiving.domain.enums.ReceivingStatus;
import com.supplychain.wms.receiving.domain.model.Receiving;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceivingRepository extends JpaRepository<Receiving, Long>, JpaSpecificationExecutor<Receiving> {

    boolean existsByReceivingNumberAndDeletedFalse(String receivingNumber);

    Optional<Receiving> findByIdAndDeletedFalse(Long id);

    Optional<Receiving> findByReceivingNumberAndDeletedFalse(String receivingNumber);

    List<Receiving> findByAdvanceShippingNoticeIdAndDeletedFalse(Long asnId);

    List<Receiving> findByPurchaseOrderIdAndDeletedFalse(Long purchaseOrderId);

    Page<Receiving> findAllByDeletedFalse(Pageable pageable);

    Page<Receiving> findByReceivingStatusAndDeletedFalse(ReceivingStatus status, Pageable pageable);
}
