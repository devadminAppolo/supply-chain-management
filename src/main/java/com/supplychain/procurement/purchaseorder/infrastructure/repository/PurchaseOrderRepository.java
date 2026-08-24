package com.supplychain.procurement.purchaseorder.infrastructure.repository;

import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {

    boolean existsByPoNumberAndDeletedFalse(String poNumber);

    boolean existsByPoNumberAndIdNotAndDeletedFalse(String poNumber, Long id);

    Optional<PurchaseOrder> findByIdAndDeletedFalse(Long id);

    Optional<PurchaseOrder> findByPoNumberAndDeletedFalse(String poNumber);

    List<PurchaseOrder> findAllByDeletedFalseOrderByPoNumberAsc();
}
