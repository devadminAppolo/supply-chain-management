package com.supplychain.procurement.purchaseorder.infrastructure.repository;

import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrderLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderLineItemRepository extends JpaRepository<PurchaseOrderLineItem, Long>, JpaSpecificationExecutor<PurchaseOrderLineItem> {

    List<PurchaseOrderLineItem> findByPurchaseOrderIdAndDeletedFalseOrderByLineNumberAsc(Long purchaseOrderId);
}
