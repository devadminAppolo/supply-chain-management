package com.supplychain.procurement.purchaseorder.infrastructure.repository;

import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrderLineItem;
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
public interface PurchaseOrderLineItemRepository extends JpaRepository<PurchaseOrderLineItem, Long>, JpaSpecificationExecutor<PurchaseOrderLineItem> {

    List<PurchaseOrderLineItem> findByPurchaseOrderIdAndDeletedFalseOrderByLineNumberAsc(Long purchaseOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT li FROM PurchaseOrderLineItem li WHERE li.id = :id AND li.deleted = false")
    Optional<PurchaseOrderLineItem> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT li FROM PurchaseOrderLineItem li WHERE li.id IN :ids AND li.deleted = false")
    List<PurchaseOrderLineItem> findAllByIdInForUpdate(@Param("ids") List<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT li FROM PurchaseOrderLineItem li WHERE li.purchaseOrder.id = :purchaseOrderId AND li.deleted = false ORDER BY li.lineNumber ASC")
    List<PurchaseOrderLineItem> findByPurchaseOrderIdForUpdate(@Param("purchaseOrderId") Long purchaseOrderId);
}
